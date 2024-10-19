import { IonAlert, IonCol, IonContent, IonFab, IonFabButton, IonGrid, IonHeader, IonIcon, IonLabel, IonLoading, IonPage, IonRow, IonTitle, IonToolbar } from "@ionic/react";
import Game from "./Game";
import "./GameList.css";
import { RouteComponentProps, useHistory } from "react-router";
import { getLogger } from "../core";
import { add } from "ionicons/icons";
import { useGetGamesQuery } from "./GameStopApi";

const log = getLogger('GameList');

const GameList: React.FC<RouteComponentProps> = ({ history, location }) => {
    const { data, error, isFetching } = useGetGamesQuery()
    const locationState = location.state as { error: Error };

    function handleAddGame() {
        history.push({
            pathname: '/gamestop/game',
            state: { game: undefined },
        })
    }

    log('render');
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Games</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                <IonAlert isOpen={locationState?.error !== undefined} header={"Error"} message={locationState?.error?.message} buttons={["OK"]} onDidDismiss={() => history.replace('/gamestop/games')} />
                <IonGrid>
                    <IonLoading isOpen={isFetching} message="Fetching games..." />
                    {
                        data && (
                            <IonRow class="ion-justify-content-start ion-align-items-start">
                                {data.map((game) => (
                                    <IonCol key={game._id} size-xs="12" size-md="6" size-lg="4">
                                        <Game {...game} />
                                    </IonCol>
                                ))}
                            </IonRow>
                        )
                    }
                    {
                        error && (<IonLabel color={"danger"}>Failed to fetch games!</IonLabel>)
                    }
                </IonGrid>
                <IonFab vertical="bottom" horizontal="end" slot="fixed">
                    <IonFabButton color={"tertiary"} onClick={handleAddGame}>
                        <IonIcon icon={add} />
                    </IonFabButton>
                </IonFab>
            </IonContent>
        </IonPage>
    );
};

export default GameList;