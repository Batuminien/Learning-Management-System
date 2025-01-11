import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getProfilePhoto } from "../../../services/profilePhotoService";
import PhotoCard from "./PhotoCard";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const Settings = () => {
    const { user } = useContext(AuthContext);

    const [photo, setPhoto] = useState(null);

    useEffect(() => {
        const fetchPhoto = async () => {
            try{
                const response = await getProfilePhoto(user.accessToken);
                console.log(response);
                setPhoto(BASE_URL + response.data.data.photoUrl);
            }catch(error){
                console.log(error);
            }
        }
        fetchPhoto();
    }, []);

    return(
        <>
            <PhotoCard
                url={photo}
                onPhotoChange={(newUrl) => setPhoto(newUrl)}
            />
            
        </>
    );
}
export default Settings;