import axios from "axios"
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
const PHOTO_URL = BASE_URL + '/api/v1/profile-photo'


export const uploadProfilePhoto = async (photo) => {
    const response = await axios.post(
        `${PHOTO_URL}`,
        photo,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return response;
}

export const getProfilePhoto = async (userID) => {
    const response = await axios.get(
        `${PHOTO_URL}/${userID}/file`,
        {
            responseType: 'blob',
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`,
            },
        }
    );
    return URL.createObjectURL(response.data);
}

export const deleteProfilePhoto = async () => {
    const response = await axios.delete(
        `${PHOTO_URL}`,
        {
            headers : {
                Authorization : `Bearer ${JSON.parse(sessionStorage.getItem('accessToken'))}`
            }
        }
    );
    return response;
}