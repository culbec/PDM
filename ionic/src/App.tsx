import { Route, Switch } from 'react-router-dom';
import { IonApp, IonRouterOutlet, setupIonicReact } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/**
 * Ionic Dark Mode
 * -----------------------------------------------------
 * For more info, please see:
 * https://ionicframework.com/docs/theming/dark-mode
 */

/* import '@ionic/react/css/palettes/dark.always.css'; */
/* import '@ionic/react/css/palettes/dark.class.css'; */
import '@ionic/react/css/palettes/dark.system.css';

/* Theme variables */
import './theme/variables.css';
import GameList from './game/GameList';
import GameDetail from './game/GameDetail';
import PrivateRoute from './core/PrivateRoute';
import Login from './auth/Login';
import { getLogger } from './core';
import { useSelector } from 'react-redux';
import React from "react";
import Register from "./auth/Register";
import { GameStopState } from './core/GameStopStore';
import { isEqual } from 'lodash';
import useSyncService from './core/useSyncService';

setupIonicReact();

const log = getLogger("App");

const App: React.FC = () => {
  log("App start");

  const isAuthenticated = useSelector((state: GameStopState) => state.auth.isAuthenticated, isEqual);
  log("isAuthenticated", isAuthenticated);

  useSyncService();

  return (
    <IonApp>
      <IonReactRouter>
        <IonRouterOutlet>
          <Switch>
            <Route exact
              path="/gamestop/login"
              component={isAuthenticated ? GameList : Login}
            />
            <Route exact
              path="/gamestop/register"
              component={isAuthenticated ? GameList : Register}
            />
            <PrivateRoute
              key="games"
              element={GameList}
              isAuthenticated={isAuthenticated}
              exact
              path="/gamestop/games" />
            <PrivateRoute
              key="game-detail"
              element={GameDetail}
              isAuthenticated={isAuthenticated}
              exact
              path="/gamestop/game/" />
            <Route component={isAuthenticated ? GameList : Login} />
          </Switch>
        </IonRouterOutlet>
      </IonReactRouter>
    </IonApp>
  )
};

export default App;