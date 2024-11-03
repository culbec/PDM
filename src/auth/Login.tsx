import { useState } from "react";
import { useLoginMutation } from "./AuthApi";
import { getLogger } from "../core";
import { IonAlert, IonButton, IonContent, IonHeader, IonInput, IonItem, IonLabel, IonPage, IonTitle, IonToolbar } from "@ionic/react";
import { RouteComponentProps } from "react-router";

const log = getLogger("Login");

interface LoginProps extends RouteComponentProps { }

const Login: React.FC<LoginProps> = ({ history, location }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [login, loginResult] = useLoginMutation();

    const locationState = location.state as { error: Error };

    const handleLogin = () => {
        log("handleLogin");
        login({ username, password });

        if (loginResult.isSuccess) {
            log("loginResult", loginResult.data);
            history.push({
                pathname: "/gamestop/games",
                state: { username: username },
            })
        } else {
            log("loginError", loginResult.error);
            history.push({
                pathname: "/gamestop/auth",
                state: { error: loginResult.error },
            })
        }
    };

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Login</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                <IonAlert isOpen={locationState?.error !== undefined} header={"Error"} message={locationState?.error?.message} buttons={["OK"]} />
                <IonItem>
                    <IonLabel position="floating">Username</IonLabel>
                    <IonInput value={username} onIonChange={e => setUsername(e.detail.value!)} />
                </IonItem>
                <IonItem>
                    <IonLabel position="floating">Password</IonLabel>
                    <IonInput type="password" value={password} onIonChange={e => setPassword(e.detail.value!)} />
                </IonItem>
                <IonButton expand="full" onClick={handleLogin}>Login</IonButton>
            </IonContent>
        </IonPage>
    )
}

export default Login;