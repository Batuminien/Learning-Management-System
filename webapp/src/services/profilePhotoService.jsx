import axios from "axios"
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
const PHOTO_URL = BASE_URL + '/api/v1/profile-photo'


export const uploadProfilePhoto = async (photo, accessToken) => {
    const response = await axios.post(
        `${PHOTO_URL}`,
        photo,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const getProfilePhoto = async (accessToken) => {
    const response = await axios.get(
        `${PHOTO_URL}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`,
            },
        }
    );
    return response;
}

export const deleteProfilePhoto = async (accessToken) => {
    const response = await axios.delete(
        `${PHOTO_URL}`,
        {
            headers : {
                Authorization : `Bearer ${accessToken}`
            }
        }
    );
    return response;
}