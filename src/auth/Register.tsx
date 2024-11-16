import { getErrorMessage, getLogger } from "../core";
import React, { useCallback } from "react";
import { useRegisterMutation } from "./AuthApi";
import {
    IonAlert,
    IonButton,
    IonContent,
    IonHeader,
    IonInput, IonItem,
    IonLabel, IonLoading,
    IonPage,
    IonTitle,
    IonToolbar
} from "@ionic/react";
import { useForm } from "react-hook-form";
import { RouteComponentProps } from "react-router";
import { useDispatch, useSelector } from "react-redux";
import { clearAuthError, setAuthError, setToken } from "./AuthSlice";
import { GameStopState } from "../core/GameStopStore";

const log = getLogger("Register");

const Register: React.FC<RouteComponentProps> = ({ history }) => {
    const dispatch = useDispatch();

    const { authError } = useSelector((state: GameStopState) => state.auth);

    const { handleSubmit, setValue, register, getValues } = useForm({
        defaultValues: {
            username: "",
            password: "",
            confirmPassword: ""
        }
    });

    const [registerMutation, registerResult] = useRegisterMutation();

    const handleRegister = useCallback(async () => {
        log("handleRegister");

        const { username, password, confirmPassword } = getValues();

        if (!username || !password || !confirmPassword) {
            dispatch(setAuthError("Please provide username, password and confirm password"));
            return;
        }

        log("passed credentials", username, password, confirmPassword);

        try {
            const result = await registerMutation({ username, password }).unwrap();

            log("handleRegister result", result);

            if (result.token === undefined) {
                dispatch(setAuthError("No token provided"));
                return;
            }

            log("handleLogin token", result.token);

            dispatch(setToken(result.token));
            history.push("/gamestop/games");
        } catch (error: any) {
            log("handleRegister error", error);
            const errorMessage = getErrorMessage(error, "Register failed");
            dispatch(setAuthError(errorMessage));
        }

        history.push("/gamestop/games");
    }, [registerMutation, dispatch, history]);

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Register</IonTitle>
                    <IonButton slot="end" onClick={() => history.push("/gamestop/login")}>Login</IonButton>
                    <IonButton slot="end" onClick={() => history.push("/gamestop/register")}>Register</IonButton>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                <IonAlert
                    isOpen={authError !== null}
                    header={"Error"}
                    message={authError!}
                    buttons={[{
                        text: "OK",
                        handler: () => dispatch(clearAuthError())
                    }]}
                />
                <IonLoading isOpen={registerResult.isLoading} message="Registering..." />
                <form onSubmit={handleSubmit(handleRegister)}>
                    <IonItem>
                        <IonLabel position="floating">Username</IonLabel>
                        <IonInput {...register("username", {
                            required: "Username is required",
                            minLength: { value: 1, message: "Username must have at least 1 character" },
                            maxLength: { value: 20, message: "Username must have at most 20 characters" },
                            pattern: {
                                value: /^[a-zA-Z]+[a-zA-Z0-9]*$/,
                                message: "Username must contain only letters and numbers"
                            }
                        })}
                            onIonChange={e => setValue("username", e.detail.value!)}
                            type="text"
                            className="ion-padding-top ion-padding-start" />
                    </IonItem>
                    <IonItem>
                        <IonLabel position="floating">Password</IonLabel>
                        <IonInput {...register("password", {
                            required: "Password is required",
                            minLength: { value: 1, message: "Password must have at least 1 character" },
                            maxLength: { value: 20, message: "Password must have at most 20 characters" },
                            pattern: {
                                value: /^[a-zA-Z0-9]*$/,
                                message: "Password must contain only letters and numbers"
                            }
                        })}
                            onIonChange={e => setValue("password", e.detail.value!)}
                            type="password"
                            className="ion-padding-top ion-padding-start" />
                    </IonItem>
                    <IonItem>
                        <IonLabel position="floating">Confirm Password</IonLabel>
                        <IonInput {...register("confirmPassword", {
                            required: "Confirm password is required",
                            validate: value => value === getValues("password") || "Passwords do not match"
                        })}
                            onIonChange={e => setValue("confirmPassword", e.detail.value!)}
                            type="password"
                            className="ion-padding-top ion-padding-start" />
                    </IonItem>
                    <IonButton className="ion-margin" expand="block" type="submit">Register</IonButton>
                </form>
            </IonContent>
        </IonPage>
    );
}

export default Register;