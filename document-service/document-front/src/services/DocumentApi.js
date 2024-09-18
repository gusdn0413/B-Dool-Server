const API_BASE_URL = "http://localhost:8080/api/documents"; // 백엔드 서버 URL

// 파일 업로드 API 호출
export const uploadDocument = async (file) => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API_BASE_URL}/upload`, {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        throw new Error('Failed to upload document');
    }

    return response.json(); // 업로드된 Document 객체를 JSON으로 반환
};

// 특정 문서의 HTML 콘텐츠를 가져오는 API
export const getDocumentContent = async (id) => {
    const response = await fetch(`${API_BASE_URL}/${id}`);

    if (!response.ok) {
        throw new Error('Failed to fetch document content');
    }

    return response.text(); // HTML 콘텐츠를 문자열로 반환
};

// 문서 콘텐츠 업데이트 API
export const updateDocumentContent = async (id, updatedContent) => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({content: updatedContent}), // 업데이트된 콘텐츠 전송
    });

    if (!response.ok) {
        throw new Error('Failed to update document');
    }

    return response.json(); // 업데이트된 Document 객체를 반환
};