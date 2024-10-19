import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon, IonCardSubtitle, IonLabel, IonToolbar } from "@ionic/react";
import { GameProps } from "./GameProps";
import { gameController } from "ionicons/icons";
import "./Game.css";
import { useHistory } from "react-router";
import { memo } from "react";

const Game: React.FC<GameProps> = memo((props) => {
    const history = useHistory();

    const handleCardClick = () => {
        history.push({
            pathname: '/gamestop/game/',
            state: { game: props }
        });
    };

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
});

export default Game;