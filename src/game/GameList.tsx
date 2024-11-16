import { InfiniteScrollCustomEvent, IonAlert, IonButton, IonCol, IonContent, IonFab, IonFabButton, IonGrid, IonHeader, IonIcon, IonInfiniteScroll, IonInfiniteScrollContent, IonItem, IonLabel, IonList, IonLoading, IonPage, IonRow, IonSearchbar, IonSelect, IonSelectOption, IonText, IonTitle, IonToast, IonToolbar } from "@ionic/react";
import Game from "./Game";
import { RouteComponentProps } from "react-router";
import { getErrorMessage, getLogger, newWebSocket, WSProps } from "../core";
import { add } from "ionicons/icons";
import { useGetGamesQuery } from "./GameApi";
import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { GameCategory, GameProps } from "./GameProps";
import { useDispatch, useSelector } from "react-redux";
import { GameStopState } from "../core/GameStopStore";
import { clearErrors, clearInfoMessage, clearNotification, setError, setNotification, setSelectedGame } from "./GameSlice";
import "./style/GameList.css";
import { isEqual } from "lodash";
import useNetwork from "../core/useNetwork";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";
import { SerializedError } from "@reduxjs/toolkit";
import { clearToken, setAuthError } from "../auth/AuthSlice";
import { useLogoutMutation } from "../auth/AuthApi";

const log = getLogger('GameList');
const ITEMS_PER_PAGE = 5;

const GameList: React.FC<RouteComponentProps> = ({ history }) => {
    const dispatch = useDispatch();
    const { networkStatus } = useNetwork();
    const [logoutMutation] = useLogoutMutation();
    const wsRef = useRef<WSProps | null>(null);

    const { notification, uiErrors, infoMessage } = useSelector((state: GameStopState) => ({
        uiErrors: state.game.uiErrors,
        infoMessage: state.game.infoMessage,
        notification: state.game.notification,
    }), isEqual);

    const { isAuthenticated } = useSelector((state: GameStopState) => state.auth, isEqual);

    const [games, setGames] = useState<GameProps[]>([]);
    const { data, error, isLoading, refetch } = useGetGamesQuery();

    const [searchFilters, setSearchFilters] = useState({
        title: '',
        category: '',
    })
    const [page, setPage] = useState(1);

    // Fetch items effect
    useEffect(() => {
        if (error) {
            log('useEffect error', error);

            const errorMessage = getErrorMessage(error, 'Failed to fetch games!');

            if ((error as FetchBaseQueryError).status === 401 || (error as SerializedError).code?.includes('401')) {
                log("Unauthorized -> logging out");

                dispatch(clearToken());
                history.push('/gamestop/login');
                return;
            } else {
                dispatch(setError({ type: 'fetchError', message: errorMessage }));
                return;
            }
        }

        log('useEffect isLoading', isLoading);
        if (!isLoading && data) {
            log('useEffect data', data);
            setGames(data);
        }

    }, [data, error, isLoading, refetch, dispatch, clearToken]);

    // Refetch on authentication
    useEffect(() => {
        if (isAuthenticated) {
            refetch();
        }
    }, [isAuthenticated, refetch]);

    // WS Effect
    useEffect(() => {
        log('useEffect WS');

        if (!isAuthenticated) {
            return;
        }

        let cancelled = false;
        const token = localStorage.getItem('token');

        log(token, wsRef.current);

        if (token && !wsRef.current) {
            wsRef.current = newWebSocket(token, (message) => {
                if (cancelled) {
                    return;
                }

                if (message.type === 'notification') {
                    dispatch(setNotification(message.payload));
                } else if (message.type === 'error') {
                    dispatch(setError({ type: 'fetchError', message: message.payload }));
                }
            });
        }

        return () => {
            log('useEffect WS cleanup');
            cancelled = true;

            if (wsRef.current) {
                wsRef.current.ws.close();
                wsRef.current = null;
            }
        }
    }, [isAuthenticated]);

    const filteredGames = useMemo(() => {
        log('searchFilters', searchFilters);

        if (!games) {
            return [];
        }

        const filtered = games.filter((game) => {
            const titleMatch = !searchFilters.title || game.title.toLowerCase().includes(searchFilters.title.toLowerCase());
            const categoryMatch = !searchFilters.category || game.category === searchFilters.category;

            return titleMatch && categoryMatch;
        }).splice(0, page * ITEMS_PER_PAGE);

        log('filtered', filtered);
        return filtered;
    }, [games, searchFilters, page]);

    const handleFilterChange = (filter: string, value: string) => {
        setSearchFilters(prev => ({
            ...prev,
            [filter]: value
        }));
        setPage(1);
    };

    const handleLoadMore = useCallback((e: InfiniteScrollCustomEvent) => {
        setPage(prev => prev + 1);
        setTimeout(() => e.target!.complete(), 3000);
    }, []);

    const hasMore = useMemo(() => {
        return games && filteredGames.length < games.length;
    }, [games, filteredGames]);

    const handleAddGame = useCallback(() => {
        log('handleAddGame');

        // setting the selected game to null, as we need new information for the new game
        dispatch(setSelectedGame(null));
        history.push('/gamestop/game');
    }, [dispatch, history]);

    const handleLogout = useCallback(async () => {
        log('handleLogout');

        try {
            const result = await logoutMutation().unwrap();

            log('handleLogout result', result);
        } catch (error: any) {
            log('handleLogout error', error);
            const errorMessage = getErrorMessage(error, 'Logout failed');
            dispatch(setAuthError(errorMessage));
        }

        if (wsRef.current) {
            wsRef.current.sendLogoutMessage();
        }

        dispatch(clearToken());
        history.push('/gamestop/login');
    }, [history, logoutMutation, dispatch]);

    log('render');
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle
                        slot="start"
                    >Games</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                <IonToolbar>
                    <IonList className="ion-padding">
                        <IonText
                            className="ion-padding"
                            style={{ textAlign: "center" }}
                            slot="end">Network: {networkStatus?.isConnected ? 'Connected' : 'Disconnected'} | Connection: {networkStatus.connectionType}</IonText>
                        <IonButton
                            className="ion-padding"
                            style={{ textAlign: "center" }}
                            fill="clear"
                            onClick={handleLogout}>Logout</IonButton>
                        <IonSearchbar value={searchFilters.title} onIonChange={(e) => handleFilterChange("title", e.detail.value!)} placeholder="Search by title..." />
                        <IonSelect name="category"
                            label="Category"
                            labelPlacement="floating"
                            value={searchFilters.category}
                            onIonChange={(e) => handleFilterChange("category", e.detail.value!)}>
                            {GameCategory.map((category) => (
                                <IonSelectOption key={category} value={category}>{category}</IonSelectOption>
                            ))}
                        </IonSelect>
                    </IonList>
                </IonToolbar>
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
                <IonLoading isOpen={isLoading} message="Fetching games..." />
                <div>
                    <IonGrid>
                        {
                            filteredGames && (
                                <IonRow class="ion-justify-content-start ion-align-items-start">
                                    {filteredGames.map((game) => (
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
                    <IonInfiniteScroll
                        onIonInfinite={handleLoadMore}
                        disabled={!hasMore}
                        threshold="100px">
                        <IonInfiniteScrollContent loadingText="Loading more games..." loadingSpinner="bubbles" />
                    </IonInfiniteScroll>
                </div>
                <IonToast
                    isOpen={notification !== undefined}
                    message={notification ?? ''}
                    duration={3000}
                    onDidDismiss={() => dispatch(clearNotification())}
                />
                <IonFab vertical="bottom" horizontal="end" slot="fixed">
                    <IonFabButton color={"tertiary"} onClick={handleAddGame}>
                        <IonIcon icon={add} />
                    </IonFabButton>
                </IonFab>
            </IonContent>
        </IonPage >
    );
};

export default memo(GameList);