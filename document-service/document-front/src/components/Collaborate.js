import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom'; // useParams 훅을 가져옵니다.
import TinyMCEEditor from './TinyMCEEditor'; // TinyMCE 기반 에디터
import {getDocumentContent} from '../services/DocumentApi';

const Collaborate = () => {
    const {id: documentId} = useParams(); // URL에서 문서 ID를 가져옴
    const [content, setContent] = useState('');

    // 서버에서 문서 내용 불러오기
    useEffect(() => {
        const fetchDocument = async () => {
            const documentContent = await getDocumentContent(documentId);
            setContent(documentContent); // 서버에서 문서 내용 가져와서 상태에 저장
        };

        fetchDocument();
    }, [documentId]);

    return (
        <div>
            <h2>실시간 공동 작업</h2>
            {/* TinyMCE 에디터에 초기 콘텐츠 전달 */}
            <TinyMCEEditor initialContent={content} documentId={documentId}/>
        </div>
    );
};

export default Collaborate;