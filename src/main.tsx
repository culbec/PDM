import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import { gameStore } from './game/GameStore';
import { Provider } from 'react-redux';

const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
  <React.StrictMode>
    <Provider store={gameStore}>
      <App />
    </Provider>
  </React.StrictMode>
);