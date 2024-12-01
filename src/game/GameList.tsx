import {
    InfiniteScrollCustomEvent,
    IonAlert,
    IonButton,
    IonCard,
    IonCardContent,
    IonCol,
    IonContent,
    IonFab,
    IonFabButton,
    IonGrid,
    IonHeader,
    IonIcon,
    IonInfiniteScroll,
    IonInfiniteScrollContent,
    IonList,
    IonLoading,
    IonPage,
    IonRow,
    IonSearchbar,
    IonSelect,
    IonSelectOption,
    IonText,
    IonTitle,
    IonToast,
    IonToolbar
} from "@ionic/react";
import Game from "./Game";
import { RouteComponentProps } from "react-router";
import { getErrorMessage, getLogger, newWebSocket, WSProps } from "../core";
import { add } from "ionicons/icons";
import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { GameCategory } from "./GameProps";
import { useDispatch, useSelector } from "react-redux";
import { GameStopState } from "../core/GameStopStore";
import { clearErrors, clearInfoMessage, clearNotification, setError, setLoadingGames, setNotification, setSelectedGame } from "./GameSlice";
import "../styles/GameList.css";
import "../styles/animations.css";
import { isEqual } from "lodash";
import useNetwork from "../core/useNetwork";
import { clearToken, setAuthError } from "../auth/AuthSlice";
import { useLogoutMutation } from "../auth/AuthApi";
import { usePhoto } from "../photo/usePhoto";
import { useGames } from "./useGames";
import { setIsFirstLoad } from "../photo/PhotoSlice";
import { useMyLocation } from "../maps/useMyLocation";
import MyMap from "../maps/MyMap";

const log = getLogger('GameList');
const ITEMS_PER_PAGE = 5;

const GameList: React.FC<RouteComponentProps> = ({ history }) => {
    const dispatch = useDispatch();
    const { networkStatus } = useNetwork();
    const [logoutMutation] = useLogoutMutation();
    const wsRef = useRef<WSProps | null>(null);

    const { notification, uiErrors, infoMessage, isAuthenticated } = useSelector((state: GameStopState) => ({
        uiErrors: state.game.uiErrors,
        infoMessage: state.game.infoMessage,
        notification: state.game.notification,
        isAuthenticated: state.auth.isAuthenticated
    }), isEqual);

    const [searchFilters, setSearchFilters] = useState({
        title: '',
        category: '',
    })
    const [page, setPage] = useState(1);

    const { games } = useGames();
    const { loadingGames } = useSelector((state: GameStopState) => state.game, isEqual);
    const { loadingPhotos } = useSelector((state: GameStopState) => state.photo, isEqual);
    usePhoto();

    const location = useMyLocation();

    const [isLoading, setIsLoading] = useState(false);

    // Track state loading
    useEffect(() => {
        const isLoading = loadingGames || loadingPhotos;
        setIsLoading(isLoading);

        return () => {
            setIsLoading(false);
        }
    }, [loadingGames, loadingPhotos]);

    // WS Effect
    useEffect(() => {
        log('useEffect WS');

        if (!isAuthenticated) {
            return;
        }

        const token = localStorage.getItem('token');

        if (token && !wsRef.current) {
            wsRef.current = newWebSocket(token, (message) => {
                if (message.type === 'notification') {
                    dispatch(setNotification(message.payload));
                } else if (message.type === 'error') {
                    dispatch(setError({ type: 'fetchError', message: message.payload }));
                }
            });
        }

        return () => {
            log('useEffect WS cleanup');

            if (wsRef.current) {
                wsRef.current.ws.close();
                wsRef.current = null;
            }
        };
    }, []);

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
        dispatch(setIsFirstLoad(true));
        setTimeout(() => history.push('/gamestop/login'), 300);
    }, [history, logoutMutation, dispatch]);

    log('render');
    log(`loadingStatuses - loadingGames: ${loadingGames}, loadingPhotos: ${loadingPhotos}`);
    return (
        <IonPage className="page-transition">
            <IonHeader>
                <IonToolbar>
                    <IonTitle slot="start">Games</IonTitle>
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
                            className="ion-padding animated-button-squash"
                            style={{ textAlign: "center" }}
                            fill="clear"
                            onClick={handleLogout}>Logout</IonButton>
                        <IonSearchbar
                            value={searchFilters.title}
                            onIonChange={(e) => handleFilterChange("title", e.detail.value!)}
                            placeholder="Search by title..." />
                        <IonSelect
                            name="category"
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
                <IonLoading isOpen={isLoading} message="Loading..." />
                <div>
                    <IonCard className="centered-card ion-margin">
                        <IonCardContent>
                            {
                                location.loading ?
                                    <IonText>Loading location...</IonText> :
                                    <MyMap
                                        latitude={location.position?.coords.latitude ?? 0}
                                        longitude={location.position?.coords.longitude ?? 0}
                                        onMapClick={() => log("onMapClick")}
                                        onMarkerClick={(e: { latitude: number, longitude: number }) => {
                                            log("onMarkerClick", e);

                                            // Find the game with the same location
                                            const game = games?.find(g => g.location.latitude === e.latitude && g.location.longitude === e.longitude);

                                            if (game) {
                                                dispatch(setSelectedGame(game));
                                                setTimeout(() => history.push("/gamestop/game"), 300);
                                            }
                                        }}
                                        mode="viewOnly"
                                        games={games}
                                    />
                            }
                        </IonCardContent>
                    </IonCard>
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
                    <IonFabButton
                        className="animated-button-spin"
                        color={"tertiary"}
                        onClick={handleAddGame}>
                        <IonIcon icon={add} />
                    </IonFabButton>
                </IonFab>
            </IonContent>
        </IonPage >
    );
};

export default memo(GameList);