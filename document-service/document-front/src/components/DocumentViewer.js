import React, {useEffect, useState} from 'react';
import {getDocumentContent} from '../services/DocumentApi'; // 서버에서 HTML 문서를 불러오는 함수

const DocumentViewer = ({documentId}) => {
    const [content, setContent] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchDocumentContent = async () => {
            try {
                const htmlContent = await getDocumentContent(documentId); // 문서 콘텐츠 불러오기
                setContent(htmlContent);  // HTML 콘텐츠를 상태에 저장
            } catch (error) {
                setError('Failed to load document content');
            }
        };

        fetchDocumentContent();
    }, [documentId]);

    if (error) {
        return <p style={{color: 'red'}}>{error}</p>;
    }

    return (
        <div className="document-viewer">
            <h2>문서 보기</h2>
            <div
                className="document-content"
                dangerouslySetInnerHTML={{__html: content}} // HTML 내용을 그대로 렌더링
            />
        </div>
    );
};

export default DocumentViewer;