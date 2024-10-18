import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonButton, IonIcon, IonCardSubtitle, IonLabel, IonToolbar } from "@ionic/react";
import { GameProps } from "./GameProps";
import { gameController } from "ionicons/icons";
import "./Game.css";
import { useHistory } from "react-router";
import { useState } from "react";

interface GameComponentProps extends GameProps {
    rentGame: () => void;
}

const Game: React.FC<GameComponentProps> = (props) => {
    const history = useHistory();
    const [isButtonHovered, setIsButtonHovered] = useState(false);

    const handleCardClick = () => {
        history.push(`/gamestop/games/${props._id}`);
    }


    return (
        <IonCard className="game-card" onClick={() => {
            if (isButtonHovered) {
                return;
            }
            handleCardClick();
        }}>
            <IonCardHeader>
                <IonToolbar>
                    <div className="game-title-container">
                        <IonCardTitle>{props.title}</IonCardTitle>
                        <IonIcon icon={gameController} className="game-icon" />
                    </div>
                </IonToolbar>
                <IonCardSubtitle className="game-release-date">Release date: {props.release_date}</IonCardSubtitle>
            </IonCardHeader>
            <IonCardContent>
                <div className="game-rental-info">
                    <IonLabel className="game-rental-price">Rental price: {props.rental_price.toPrecision()}</IonLabel>
                    {
                        props.isRented ?
                            <>
                                <IonLabel color={"danger"} className="game-rental-status">Rented</IonLabel>
                                <IonButton
                                    onClick={props.rentGame}
                                    onMouseEnter={() => { setIsButtonHovered(true); }}
                                    onMouseLeave={() => { setIsButtonHovered(false); }}>Return Game</IonButton>
                            </> :
                            <>
                                <IonLabel color={"success"} className="game-rental-status">Available</IonLabel>
                                <IonButton
                                    onClick={props.rentGame}
                                    onMouseEnter={() => { setIsButtonHovered(true); }}
                                    onMouseLeave={() => { setIsButtonHovered(false); }}>Rent Game</IonButton>
                            </>
                    }
                </div>
            </IonCardContent>
        </IonCard>
    );
};

export default Game;