import React from 'react';
import ReactDOM from 'react-dom/client';  // React 18 버전의 createRoot API
import './index.css';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));  // createRoot 사용
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);