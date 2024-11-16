import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon, IonCardSubtitle, IonLabel, IonToolbar } from "@ionic/react";
import { GameProps } from "./GameProps";
import { gameController } from "ionicons/icons";
import { useHistory } from "react-router";
import { memo, useCallback } from "react";
import { useDispatch } from "react-redux";
import { getLogger } from "../core";
import { setSelectedGame } from "./GameSlice";
import "./style/Game.css";

const log = getLogger("Game");

const Game: React.FC<GameProps> = (props) => {
    const history = useHistory();
    const dispatch = useDispatch();

    const handleCardClick = useCallback(() => {
        log("selected game", props);
        dispatch(setSelectedGame(props));
        history.push(`/gamestop/game`);
    }, [props, dispatch, history]);

    return (
        <IonCard className="game-card" onClick={handleCardClick}>
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
    );
};

export default memo(Game);