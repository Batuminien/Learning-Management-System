import { createContext, useContext, useEffect, useState } from "react";
import useAuth from "../hooks/useAuth";
import { getProfilePhoto } from "../services/profilePhotoService";




const ProfilePhotoContext = createContext();

export const useProfilePhoto = () => useContext(ProfilePhotoContext);

export const ProfilePhotoProvider = ({children}) => {
    const [profilePhoto, setProfilePhoto] = useState(null);
    const { user } = useAuth();

    useEffect(() => {
        const fetchPhoto = async () => {
            if(!user) return;
            try{
                const response = await getProfilePhoto(user.id);
                console.log(response);
                setProfilePhoto(response);
            }catch(error){
                console.log(error);
                setProfilePhoto(null);    
            }
        }
        fetchPhoto();
    }, [user]);

    return (
        <ProfilePhotoContext.Provider value={{ profilePhoto, setProfilePhoto }}>
            {children}
        </ProfilePhotoContext.Provider>
    );
}