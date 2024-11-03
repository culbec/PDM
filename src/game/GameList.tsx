import { IonAlert, IonCol, IonContent, IonFab, IonFabButton, IonGrid, IonHeader, IonIcon, IonInfiniteScroll, IonInfiniteScrollContent, IonLabel, IonLoading, IonPage, IonRow, IonTitle, IonToolbar } from "@ionic/react";
import Game from "./Game";
import "./GameList.css";
import { RouteComponentProps } from "react-router";
import { getLogger } from "../core";
import { add } from "ionicons/icons";
import { useGetGamesQuery } from "./GameStopApi";
import { useState } from "react";

const log = getLogger('GameList');

const GameList: React.FC<RouteComponentProps> = ({ history, location }) => {
    const [page, setPage] = useState(1);
    const [filter, setFilter] = useState("");
    const [username, setUsername] = useState("");
    const { data, error, isFetching } = useGetGamesQuery({ username, page, title: filter });
    const locationState = location.state as { username: string, error: Error };

    function handleAddGame() {
        history.push({
            pathname: '/gamestop/game',
            state: { game: undefined },
        })
    }

    const loadMoreGames = (e: CustomEvent<void>) => {
        log('loadMoreGames');
        const { data: newGames, error } = useGetGamesQuery({ username, page: page + 1, title: filter });
        if (error) {
            log('error', error);
        } else {
            setPage(page + 1);
            log('newGames', newGames);
            (e.target as HTMLIonInfiniteScrollElement).complete();
        }
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
                <IonInfiniteScroll onIonInfinite={loadMoreGames} threshold="100px">
                    <IonInfiniteScrollContent loadingText="Loading more games..."></IonInfiniteScrollContent>
                </IonInfiniteScroll>
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