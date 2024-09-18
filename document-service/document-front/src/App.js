import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import DocumentUpload from './components/DocumentUpload';
import Collaborate from './components/Collaborate';

function App() {
    return (
        <Router>
            <Routes>
                {/* 업로드 페이지 */}
                <Route path="/" element={<DocumentUpload/>}/>
                {/* 공동 작업 페이지 */}
                <Route path="/collaborate/:id" element={<Collaborate/>}/>
            </Routes>
        </Router>
    );
}

export default App;