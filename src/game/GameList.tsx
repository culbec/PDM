import { IonCol, IonContent, IonFab, IonFabButton, IonGrid, IonHeader, IonIcon, IonLabel, IonLoading, IonPage, IonRow, IonTitle, IonToolbar } from "@ionic/react";
import Game from "./Game";
import { useCallback, useContext, useState } from "react";
import "./GameList.css";
import { GameContext } from "./GameProvider";
import { useHistory } from "react-router";
import { getLogger } from "../core";
import { add } from "ionicons/icons";

const log = getLogger('GameList');

const GameList: React.FC = () => {
    const history = useHistory();
    const { games, fetching, fetchingError } = useContext(GameContext)

    const rentGame = useCallback(async (id: string | undefined) => {
        console.log("renting game with id: ", id);

        // Iterate through the games array and find the game with the matching id
        const game = games?.find(game => game._id === id);

        // If the game is found, send an edit request to the API
        history.push(`/gamestop/games/${id}`);
    }, [games]);

    log('render');
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Games</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                <IonGrid>
                    <IonLoading isOpen={fetching} message="Fetching games..." />
                    {
                        games && (
                            <IonRow class="ion-justify-content-start ion-align-items-start">
                                {games.map((game) => (
                                    <IonCol key={game._id} size-xs="12" size-md="6" size-lg="4">
                                        <Game {...game} rentGame={() => rentGame(game._id)} />
                                    </IonCol>
                                ))}
                            </IonRow>
                        )
                    }
                    {
                        fetchingError && (<IonLabel color={"danger"}>{fetchingError.message || "Failed to fetch games!"}</IonLabel>)
                    }
                </IonGrid>
                <IonFab vertical="bottom" horizontal="end" slot="fixed">
                    <IonFabButton color={"tertiary"} onClick={() => history.push("/gamestop/games/new")}>
                        <IonIcon icon={add} />
                    </IonFabButton>
                </IonFab>
            </IonContent>
        </IonPage>
    );
};

export default GameList;