import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom'; // useNavigate 사용
import {uploadDocument} from '../services/DocumentApi'; // API 호출 함수

const DocumentUpload = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const navigate = useNavigate(); // useHistory 대신 useNavigate 사용

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            alert("파일을 선택하세요.");
            return;
        }

        try {
            const response = await uploadDocument(selectedFile);
            // 서버에서 공동 작업 URL을 받아서 리디렉션
            if (response.url) {
                navigate(response.url); // 공동 작업 페이지로 리디렉션
            }
        } catch (error) {
            console.error("문서 업로드 실패", error);
        }
    };

    return (
        <div>
            <h2>문서 업로드</h2>
            <input type="file" accept="text/html" onChange={handleFileChange}/>
            <button onClick={handleUpload}>업로드</button>
        </div>
    );
};

export default DocumentUpload;