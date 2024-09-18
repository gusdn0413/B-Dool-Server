import React, {useState, useEffect} from 'react';
import {Editor} from '@tinymce/tinymce-react';
import {getDocumentContent, updateDocumentContent} from '../services/DocumentApi';
import UseWebSocketConnection from '../services/UseWebSocketConnection';

const TinyMCEEditor = ({initialContent, documentId}) => {
    const [content, setContent] = useState(initialContent || '');

    // 서버에서 문서 콘텐츠를 가져오는 부분 (파일 로드)
    useEffect(() => {
        const fetchDocumentContent = async () => {
            try {
                const fetchedContent = await getDocumentContent(documentId);
                setContent(fetchedContent);  // 서버에서 가져온 콘텐츠 설정
            } catch (error) {
                console.error('문서 불러오기 실패:', error);
            }
        };

        if (!initialContent) {
            fetchDocumentContent();
        }
    }, [documentId, initialContent]);

    // WebSocket을 통해 전체 문서를 수신할 때 호출되는 함수
    const handleMessageReceived = (fullContent) => {
        console.log("Received full document via WebSocket:", fullContent);
        setContent(fullContent); // 실시간으로 받은 전체 문서를 에디터에 업데이트
    };

    // WebSocket 연결 설정
    const {sendDocument} = UseWebSocketConnection(documentId, handleMessageReceived);

    // 에디터의 내용이 변경될 때 서버로 전체 데이터를 전송
    const handleEditorChange = (newContent) => {
        console.log("Editor content changed, updating state:", newContent);
        setContent(newContent);

        // 변경된 콘텐츠를 서버로 전송 (청크 나누기 없이 전체 전송)
        sendDocument(newContent);
    };

    // 저장 버튼 클릭 시 서버로 전체 내용 전송
    const handleSave = async () => {
        try {
            await updateDocumentContent(documentId, content);  // 서버로 전체 내용을 저장 요청
            alert('문서가 성공적으로 저장되었습니다!');
        } catch (error) {
            console.error('문서 저장 실패:', error);
            alert('문서 저장에 실패했습니다.');
        }
    };

    return (
        <div className="editor-container">
            <h2>문서 편집</h2>
            <Editor
                apiKey="vtpy6p4i8hvl8bgwxhcq9d759qthtuoaajldgn970dn7bsvx"
                value={content}
                init={{
                    height: 500,
                    menubar: false,
                    plugins: [
                        'advlist autolink lists link image charmap print preview anchor',
                        'searchreplace visualblocks code fullscreen',
                        'table',
                        'insertdatetime media table paste code help wordcount',
                        'code',
                        'emoticons',
                        'directionality',
                        'colorpicker',
                    ],
                    toolbar: 'undo redo | styleselect | bold italic underline | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | table | emoticons | directionality | code | help',
                    forced_root_block: '',  // 빈 줄에 p 태그 추가 방지
                    verify_html: false,
                    valid_elements: '*[*]',  // 모든 HTML 요소와 속성 허용
                }}
                onEditorChange={handleEditorChange}  // 에디터 내용 변경 시 전체 문서 전송
            />
            <button onClick={handleSave} className="save-button">저장</button>
        </div>
    );
};

export default TinyMCEEditor;
// apiKey="vtpy6p4i8hvl8bgwxhcq9d759qthtuoaajldgn970dn7bsvx"