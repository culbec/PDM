import { IonPage, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent, IonInput, IonDatetime, IonCheckbox, IonCard, IonCardContent, IonCardTitle, IonCardHeader, IonLabel } from "@ionic/react";
import { RouteComponentProps } from "react-router";
import { getLogger } from "../core";
import { useContext, useState, useEffect, useCallback } from "react";
import { GameContext } from "./GameProvider";
import { GameProps } from "./GameProps";
import "./GameDetail.css";

interface GameDetailProps extends RouteComponentProps<{ id?: string }> { }

const log = getLogger('GameDetail');

const GameDetail: React.FC<GameDetailProps> = ({ history, match }) => {
    const { games, fetching, fetchingError, saveGame } = useContext(GameContext);
    const [game, setGame] = useState<GameProps>();
    const [isNewGame, setIsNewGame] = useState<boolean>(false);

    const [title, setTitle] = useState<string>('');
    const [releaseDate, setReleaseDate] = useState<string>('');
    const [rentalPrice, setRentalPrice] = useState<number>(0);
    const [isRented, setIsRented] = useState<boolean>(false);

    useEffect(() => {
        log('useEffect');

        const routeId = match.params.id || 'new';

        if (routeId === 'new') {
            log('New Game');
            setIsNewGame(true);
        }

        const game = games?.find(it => it._id === routeId);
        if (game) {
            setGame(game);
            setTitle(game.title);
            setReleaseDate(game.release_date);
            setRentalPrice(game.rental_price);
            setIsRented(game.isRented);
        }
    }, [match.params.id, games]);

    const handleSave = useCallback(() => {
        log('handleSave');
        const updatedGame: GameProps = {
            ...game,
            title,
            release_date: releaseDate,
            rental_price: rentalPrice,
            isRented,
        };
        saveGame && saveGame(updatedGame).then(() => history.push('/gamestop/games'));
    }, [title, releaseDate, rentalPrice, isRented, saveGame, history]);

    log('render');
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>{isNewGame ? 'Add New Game' : 'Edit Game'}</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                {isNewGame ?
                    (
                        <IonCard className="centered-card">
                            <IonCardHeader>
                                <IonCardTitle>Game Details</IonCardTitle>
                            </IonCardHeader>
                            <IonCardContent>
                                <IonLabel position="stacked">Title</IonLabel>
                                <IonInput
                                    value={title}
                                    onIonChange={e => setTitle(e.detail.value!)}
                                />
                                <IonLabel position="stacked">Release Date</IonLabel>
                                <IonDatetime
                                    min="1998-01-01T00:00:00"
                                    max="2024-09-30T00:00:00"
                                    onIonChange={e => {
                                        if (e.detail.value) {
                                            // Checking if the value is either a string of a string array
                                            if (typeof e.detail.value === 'string') {
                                                setReleaseDate(e.detail.value);
                                            } else if (Array.isArray(e.detail.value) && e.detail.value.length > 0) {
                                                log(e.detail.value);
                                                setReleaseDate(e.detail.value[0]);
                                            }
                                        }

                                    }}
                                />
                                <IonLabel position="stacked">Rental Price</IonLabel>
                                <IonInput
                                    type="number"
                                    min={2.0}
                                    max={200.0}
                                    value={rentalPrice}
                                    onIonChange={e => setRentalPrice(parseFloat(e.detail.value!))}
                                />
                                <IonButton className="save-button" onClick={handleSave}>Save</IonButton>

                            </IonCardContent>
                        </IonCard>
                    ) :
                    (
                        <IonCard>

                        </IonCard>
                    )
                }
            </IonContent>
        </IonPage>
    );
};

export default GameDetail;