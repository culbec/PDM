import {
    IonPage,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButton,
    IonContent,
    IonInput,
    IonDatetime,
    IonCard,
    IonCardContent,
    IonCardTitle,
    IonCardHeader,
    IonLabel,
    IonRange,
    IonSelect,
    IonSelectOption,
    IonAlert,
    IonGrid,
    IonRow,
    IonCol,
    IonIcon,
    IonImg,
    IonActionSheet,
    IonText,
} from "@ionic/react";
import { camera, trash, close } from "ionicons/icons";
import { RouteComponentProps } from "react-router";
import { getLogger } from "../core";
import { GameCategory, GameProps } from "./GameProps";
import { useState, useCallback, memo, useRef, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { GameStopState } from "../core/GameStopStore";
import { clearErrors, clearInfoMessage, setError } from "./GameSlice";
import "../styles/GameDetail.css";
import "../styles/animations.css";
import { isEqual } from "lodash";
import useSync from "../core/useSync";
import { GamePhoto, usePhoto } from "../photo/usePhoto";
import { useMyLocation } from "../maps/useMyLocation";
import MyMap from "../maps/MyMap";


interface GameDetailProps extends RouteComponentProps {
}

const log = getLogger('GameDetail');

const initialGame: GameProps = {
    title: "GameTest",
    release_date: new Date("2012-10-20T12:00:00.000Z").toISOString(),
    rental_price: 10.5,
    rating: 5,
    category: "Action",
    location: { latitude: 37.422734313508634, longitude: -122.08532438857299 },
};

const GameDetail: React.FC<GameDetailProps> = ({ history }) => {
    const dispatch = useDispatch();
    const { startSync } = useSync(dispatch);

    const { selectedGame, uiErrors, infoMessage } = useSelector((state: GameStopState & { game: { selectedGame: GameProps, uiErrors: any, infoMessage: string } }) => ({
        selectedGame: state.game.selectedGame,
        uiErrors: state.game.uiErrors,
        infoMessage: state.game.infoMessage,
    }), isEqual);

    const [game, setGame] = useState<GameProps>(selectedGame ? selectedGame : initialGame);

    const [photoToDelete, setPhotoToDelete] = useState<GamePhoto | undefined>(undefined);
    const { photos } = useSelector((state: GameStopState) => state.photo, isEqual);
    const { takePhoto, deletePhoto } = usePhoto();

    const gamePhotos = useMemo(() => photos.filter(photo => photo.game_id === game._id), [photos, game._id]);

    const location = useMyLocation();

    const checkInputValues: (game: GameProps) => Error = useCallback((game) => {
        let error = "";

        if (game.title === "") {
            error += "Title is required\n";
        }

        if (game.release_date === "") {
            error += "Release Date is required\n";
        }

        if (game.rental_price < 2.0) {
            error += "Rental Price must be at least 2.0\n";
        }

        if (game.rental_price > 200.0) {
            error += "Rental Price must be at most 200.0\n";
        }

        if (game.rating < 0) {
            error += "Rating must be at least 0\n";
        }

        if (game.rating > 10) {
            error += "Rating must be at most 10\n";
        }

        if (game.category === "") {
            error += "Category is required\n";
        }

        return new Error(error);
    }, [game]);

    const handleSave = useCallback(async () => {
        log('handleSave');

        // Checking if all the fields are filled
        let error: Error = checkInputValues(game);
        if (error.message !== "") {
            log('handleSaveError', error);

            dispatch(setError({ type: 'saveError', message: error.message }));

            history.replace("/gamestop/games");
            return;
        }

        if (game._id) {
            startSync({ type: "update_game", payload: game });
        } else {
            startSync({ type: "save_game", payload: game });
        }
    }, [game, startSync, dispatch, history]);

    const handleInputChange = useCallback((field: keyof GameProps) => (e: CustomEvent) => {
        const inputElement = e.target as HTMLInputElement;
        let value: any = inputElement.value;
        log(`Updating ${field} with value ${value}`);
        log(game);

        if (inputElement.type === 'number') {
            value = parseFloat(value);
        }

        setGame((oldGame) => ({ ...oldGame, [field]: value }));
    }, [game]);

    log('render');
    log('selectedGame', selectedGame);
    return (
        <IonPage className="page-transition">
            <IonHeader>
                <IonToolbar>
                    <IonTitle>{game._id ? 'Edit Game' : 'Add New Game'}</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                {
                    uiErrors.fetchError !== undefined || uiErrors.saveError !== undefined
                    &&
                    <IonAlert
                        isOpen={uiErrors.fetchError !== undefined || uiErrors.saveError !== undefined}
                        header={"Error"}
                        message={uiErrors.fetchError ?? uiErrors.saveError}
                        buttons={["OK"]}
                        onDidDismiss={() => dispatch(clearErrors())} />
                }
                {
                    infoMessage !== undefined
                    &&
                    <IonAlert
                        isOpen={infoMessage !== undefined}
                        header={"Info"}
                        message={infoMessage ?? ''}
                        buttons={["OK"]}
                        onDidDismiss={() => dispatch(clearInfoMessage())} />
                }
                <IonGrid>
                    <IonRow>
                        {
                            game._id &&
                            <IonCol key="game-photos">
                                <IonCard className="centered-card ion-margin">
                                    <IonCardContent>
                                        <IonGrid>
                                            <IonRow>
                                                {gamePhotos.length === 0 && <IonCol className="ion-padding" style={{ textAlign: "center", fontSize: "1.4rem" }}>No photos!</IonCol>}
                                                {gamePhotos.map((photo, index) => (
                                                    <IonCol className="ion-padding" key={index} size="6">
                                                        <IonImg onClick={() => setPhotoToDelete(photo)} src={`data:image/jpeg;base64,${photo.data}`} />
                                                    </IonCol>
                                                ))}
                                            </IonRow>
                                        </IonGrid>
                                        <IonButton onClick={() => takePhoto!(game._id!)}>
                                            <IonIcon style={{ fontSize: "1.5rem" }} icon={camera} />
                                        </IonButton>
                                    </IonCardContent>
                                </IonCard>
                            </IonCol>
                        }
                        <IonCol key="maps">
                            <IonCard className="centered-card ion-margin">
                                <IonCardContent>
                                    {
                                        location.loading ?
                                            <IonText>Loading location...</IonText> :
                                            <MyMap
                                                latitude={game.location.latitude}
                                                longitude={game.location.longitude}
                                                onMapClick={(e: { latitude: number, longitude: number }) => {
                                                    log("onMapClick", e);
                                                    setGame((oldGame) => ({ ...oldGame, location: { latitude: e.latitude, longitude: e.longitude } }));
                                                }}
                                                onMarkerClick={() => log("onMarkerClick")}
                                                mode="addMarker"
                                            />
                                    }
                                </IonCardContent>
                            </IonCard>
                        </IonCol>
                    </IonRow>
                </IonGrid>
                <IonCard className="centered-card">
                    <IonCardHeader>
                        <IonCardTitle>Game Details</IonCardTitle>
                    </IonCardHeader>
                    <IonCardContent>
                        <IonLabel position="stacked">Title</IonLabel>
                        <IonInput
                            name="title"
                            value={game.title}
                            placeholder="Enter the title"
                            onIonChange={handleInputChange('title')}
                        />
                        <IonLabel position="stacked">Release Date</IonLabel>
                        <IonDatetime
                            name="release_date"
                            min="1998-01-01T00:00:00"
                            max="2024-09-30T00:00:00"
                            value={game.release_date ? new Date(game.release_date).toISOString() : new Date("2024-09-30T00:00:00").toISOString()}
                            onIonChange={handleInputChange('release_date')}
                        />
                        <IonLabel position="stacked">Rental Price</IonLabel>
                        <IonInput
                            name="rental_price"
                            type="number"
                            min={2.0}
                            max={200.0}
                            value={game.rental_price}
                            placeholder="Enter the rental price"
                            onIonChange={handleInputChange('rental_price')}
                        />
                        <IonLabel position="stacked">Rating</IonLabel>
                        <IonRange
                            name="rating"
                            min={0}
                            max={10}
                            value={game.rating}
                            pin={true}
                            onIonChange={handleInputChange('rating')}>
                            <IonLabel slot="start">0</IonLabel>
                            <IonLabel slot="end">10</IonLabel>
                        </IonRange>
                        <IonSelect name="category"
                            label="Category"
                            labelPlacement="floating"
                            value={game.category}
                            onIonChange={handleInputChange("category")}>
                            {GameCategory.map((category) => (
                                <IonSelectOption key={category} value={category}>{category}</IonSelectOption>
                            ))}
                        </IonSelect>
                        <IonButton onClick={handleSave}>Save</IonButton>
                    </IonCardContent>
                </IonCard>
                <IonActionSheet
                    isOpen={photoToDelete !== undefined}
                    header="Delete Photo?"
                    buttons={[
                        {
                            text: "Delete",
                            role: "destructive",
                            icon: trash,
                            handler: () => {
                                deletePhoto(photoToDelete!);
                                setPhotoToDelete(undefined);
                            },
                        },
                        {
                            text: "Cancel",
                            role: "cancel",
                            icon: close,
                        },
                    ]}
                    onDidDismiss={() => setPhotoToDelete(undefined)}
                />
            </IonContent>
        </IonPage >
    );
};

export default memo(GameDetail);

function writePhoto(photo: GamePhoto) {
    throw new Error("Function not implemented.");
}
