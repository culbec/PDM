import { createRoot } from 'react-dom/client';
import App from './App';
import { Provider } from 'react-redux';
import { gameStopStore } from './core/GameStopStore';
import { defineCustomElements } from '@ionic/pwa-elements/loader';

const container = document.getElementById('root');
const root = createRoot(container!);

defineCustomElements(window);
root.render(
  <Provider store={gameStopStore}>
    <App />
  </Provider>
);