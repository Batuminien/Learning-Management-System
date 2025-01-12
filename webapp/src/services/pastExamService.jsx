import axios from "axios"
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
const EXAM_URL = BASE_URL + '/api/v1/past-exams';

export const getStudentExams = async (studentID) => {
    const response = await axios.get(
        `${EXAM_URL}/student/${studentID}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return response.data;
}

export const getAllExams = async () => {
    const response = await axios.get(
        `${EXAM_URL}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`
            }
        }
    );
    return response.data;
}

export const updatePastExam = async (examID, updatedData) => {
    const response = await axios.put(
        `${EXAM_URL}/${examID}`,
        updatedData,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return response;
}

export const deletePastExam = async (examID) => {
    const response = await axios.delete(
        `${EXAM_URL}/${examID}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return response; 
}