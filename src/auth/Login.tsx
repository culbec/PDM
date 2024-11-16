import React, { useCallback, useState } from "react";
import { useLoginMutation } from "./AuthApi";
import { getErrorMessage, getLogger } from "../core";
import { IonAlert, IonButton, IonContent, IonHeader, IonInput, IonItem, IonLabel, IonLoading, IonPage, IonTitle, IonToolbar } from "@ionic/react";
import { RouteComponentProps } from "react-router";
import { useDispatch, useSelector } from "react-redux";
import { clearAuthError, setAuthError, setToken } from "./AuthSlice";
import { GameStopState } from "../core/GameStopStore";
import { isEqual } from "lodash";

const log = getLogger("Login");

const Login: React.FC<RouteComponentProps> = ({ history }) => {
    const dispatch = useDispatch();

    const { authError } = useSelector((state: GameStopState) => state.auth, isEqual);

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loginMutation, loginResult] = useLoginMutation();

    const handleLogin = useCallback(async () => {
        log("handleLogin");

        if (!username || !password) {
            dispatch(setAuthError("Please provide username and password"));
            return;
        }

        log("passed credentials", username, password);

        try {
            const result = await loginMutation({ username, password }).unwrap();

            log("handleLogin result", result);

            if (result.token === undefined) {
                dispatch(setAuthError("No token provided"));
                return;
            }

            log("handleLogin token", result.token);

            dispatch(setToken(result.token));
            history.push("/gamestop/games");
        } catch (error: any) {
            log("handleLogin error", error);
            const errorMessage = getErrorMessage(error, "Login failed");

            dispatch(setAuthError(errorMessage));
        }
    }, [username, password, loginMutation, dispatch, history]);

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Login</IonTitle>
                    <IonButton slot="end" onClick={() => history.push("/gamestop/login")}>Login</IonButton>
                    <IonButton slot="end" onClick={() => history.push("/gamestop/register")}>Register</IonButton>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                {
                    authError &&
                    <IonAlert
                        isOpen={authError !== null}
                        header={"Error"}
                        message={authError!}
                        buttons={[{
                            text: "OK",
                            handler: () => dispatch(clearAuthError())
                        }]}
                    />
                }
                <IonLoading isOpen={loginResult.isLoading} message="Logging in..." />
                <IonItem>
                    <IonLabel position="floating">Username</IonLabel>
                    <IonInput className="ion-padding-top ion-padding-start" value={username} onIonChange={e => setUsername(e.detail.value!)} />
                </IonItem>
                <IonItem>
                    <IonLabel position="floating">Password</IonLabel>
                    <IonInput className="ion-padding-top ion-padding-start" type="password" value={password} onIonChange={e => setPassword(e.detail.value!)} />
                </IonItem>
                <IonButton className="ion-margin" expand="block" onClick={handleLogin}>Login</IonButton>
            </IonContent>
        </IonPage>
    )
}

export default Login;