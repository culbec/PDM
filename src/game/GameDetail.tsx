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
    IonAlert
} from "@ionic/react";
import { RouteComponentProps } from "react-router";
import { getLogger } from "../core";
import { GameCategory, GameProps } from "./GameProps";
import { useState, useCallback, useEffect, memo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { GameStopState } from "../core/GameStopStore";
import { clearErrors, clearInfoMessage, setError } from "./GameSlice";
import "./style/GameDetail.css";
import { isEqual } from "lodash";
import useSync from "../core/useSync";


interface GameDetailProps extends RouteComponentProps<{ id?: string }> { }

const log = getLogger('GameDetail');

const initialGame: GameProps = {
    title: "GameTest",
    release_date: new Date("2012-10-20T12:00:00.000Z").toISOString(),
    rental_price: 10.5,
    rating: 5,
    category: "Action",
};

const GameDetail: React.FC<GameDetailProps> = ({ history }) => {
    const dispatch = useDispatch();
    const { startSync } = useSync(dispatch);

    const { selectedGame, uiErrors, infoMessage } = useSelector((state: GameStopState) => ({
        selectedGame: state.game.selectedGame,
        uiErrors: state.game.uiErrors,
        infoMessage: state.game.infoMessage,
    }), isEqual);

    const [game, setGame] = useState<GameProps>(selectedGame ? selectedGame : initialGame);

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
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>{!game._id ? 'Add New Game' : 'Edit Game'}</IonTitle>
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
            </IonContent>
        </IonPage >
    );
};

export default memo(GameDetail);