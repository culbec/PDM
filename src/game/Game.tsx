import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon, IonCardSubtitle, IonLabel, IonToolbar, IonImg, IonList } from "@ionic/react";
import { GameProps } from "./GameProps";
import { gameController } from "ionicons/icons";
import { useHistory } from "react-router";
import { memo, useCallback, useLayoutEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLogger } from "../core";
import { setSelectedGame } from "./GameSlice";
import "../styles/Game.css";
import "../styles/animations.css"
import { GameStopState } from "../core/GameStopStore";
import { isEqual } from "lodash";

const log = getLogger("Game");

const Game: React.FC<GameProps> = ({ ...props }) => {
    const history = useHistory();
    const dispatch = useDispatch();

    const { photos } = useSelector((state: GameStopState) => state.photo, isEqual);

    const gamePhotos = useMemo(() => photos.filter((photo) => photo.game_id === props._id), [photos, props._id]);

    const handleCardClick = useCallback(() => {
        dispatch(setSelectedGame(props));
        setTimeout(() => history.push("gamestop/game"), 300);
    }, [props, dispatch]);

    return (
        <div>
            <IonCard className="game-card container-wiggle" onClick={handleCardClick}>
                <IonCardHeader>
                    <IonToolbar>
                        <div className="game-title-container">
                            <IonCardTitle>{props.title}</IonCardTitle>
                            <IonIcon icon={gameController} className="game-icon" />
                        </div>
                    </IonToolbar>
                    <IonCardSubtitle className="game-release-date">Release date: {new Date(props.release_date).toDateString()}</IonCardSubtitle>
                </IonCardHeader>
                <IonCardContent>
                    <div className="game-rental-info">
                        <IonLabel>Rating: {props.rating.toPrecision()}</IonLabel>
                        <IonLabel>Category: {props.category}</IonLabel>
                        <IonLabel id="game-rental-price">Rental price: {props.rental_price.toPrecision()}</IonLabel>
                    </div>

                </IonCardContent>
            </IonCard >
            {
                gamePhotos.length > 0 &&
                <IonImg
                    key={gamePhotos[0].filepath}
                    src={`data:image/jpeg;base64,${gamePhotos[0].data}`}
                    className="img-border"
                />
            }
        </div>
    );
};

export default memo(Game);