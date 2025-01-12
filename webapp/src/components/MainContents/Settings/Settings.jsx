import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getProfilePhoto } from "../../../services/profilePhotoService";
import PhotoCard from "./PhotoCard";


const Settings = () => {
    

    return(
        <>
            <PhotoCard
            />
            
        </>
    );
}
export default Settings;