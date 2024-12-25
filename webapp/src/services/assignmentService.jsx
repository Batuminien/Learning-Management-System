import axios from "axios";
const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const createAssignment = async (assignmentData, accessToken) => {
    const response = await axios.post(
        `${BASE_URL}/api/v1/assignments/createAssignment`,
        assignmentData,
        {
            headers : {
                "Content-Type" : "application/json",
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response.data;
}

export const deleteAssignment = async (assignmentID, accessToken) => {
    const response = await axios.delete(
        `${BASE_URL}/api/v1/assignments/${assignmentID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response.data;
}

export const updateAssignment = async (assignmentID, updatedRequest, accessToken) => {
    const response = await axios.put(
        `${BASE_URL}/api/v1/assignments/${assignmentID}`,
        updatedRequest,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response.data;
}


export const uploadDocument = async (assignmentID, file, accessToken) => {
    const fileData = new FormData();
    fileData.append('file', file);

    const response = await axios.post(
        `${BASE_URL}/api/v1/assignments/${assignmentID}/documents`,
        fileData,
        {
            headers : {
                'Content-Type' : 'multipart/form-data',
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const deleteDocument = async (documentID, accessToken) => {
    const response = await axios.delete(
        `${BASE_URL}/api/v1/assignments/documents/${documentID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const downloadDocument = async (documentID, accessToken) => {
    const response = await axios.get(
        `${BASE_URL}/api/v1/assignments/documents/${documentID}`,
        {
            responseType : 'blob',
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    console.log(response)
    return response.data;
}

export const submitAssignment = async (assignmentID, fileData, accessToken) => {
    const response = await axios.patch(
        `${BASE_URL}/api/v1/assignments/${assignmentID}/submit`,
        fileData,
        {
            headers : {
                'Content-Type' : 'multipart/form-data',
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    
    return response.data;
}

export const unsubmitStudentAssignment = async (assignmentID, accessToken) => {
    try {
        const response = await axios.patch(
            `${BASE_URL}/api/v1/assignments/${assignmentID}/unsubmit`,
            {},
            {
                headers : {
                    'Content-Type' : 'application/json',
                    Authorization : `Bearer ${accessToken}`
                },
            }
        );
        return response.data;
    }catch(error) {
        console.log(error);
    }
}

export const getAssignments = async (userRole, userID, filter, accessToken) => {
    if(userRole === 'ROLE_STUDENT') {
        return await getAssignmentsForStudent(userID,accessToken);
    } else if(userRole === 'ROLE_TEACHER') {
        return await getAssignmentsForTeacher(userID, filter, accessToken);
    } else if(userRole === 'ROLE_ADMIN' || userRole === 'ROLE_COORDINATOR') {
        return await getAllAssignments(accessToken);
    }
}

export const getAssignmentsForStudent = async (studentID, accessToken) => {
    const response = await axios.get(
        `${BASE_URL}/api/v1/assignments/student/${studentID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response.data;
}

export const getAssignmentsForTeacher = async (teacherID, filter, accessToken) => {
    const {classId, courseId, dueDate} = filter;
    const response = await axios.get(
        `${BASE_URL}/api/v1/assignments/teacher/${teacherID}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
            params : {
                classId,
                courseId,
                dueDate,
            }
        }
    );
    return response.data;
}

export const getAllAssignments = async (accessToken) => {
    const response = await axios.get(
        `${BASE_URL}/api/v1/assignments`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response.data;
}

export const bulkGradeAssignment = async (assignmentID, grades, accessToken) => {
    const response = await axios.patch(
        `${BASE_URL}/api/v1/assignments/${assignmentID}/bulk-grade`,
        {
            grades : grades,
        },
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response.data;
}

export default { createAssignment, getAssignmentsForStudent };