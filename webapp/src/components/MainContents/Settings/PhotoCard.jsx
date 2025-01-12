import { useState } from "react";
import './PhotoCard.css'
import Document from "../../common/Document";
import { deleteProfilePhoto, uploadProfilePhoto } from "../../../services/profilePhotoService";
import { useProfilePhoto } from "../../../contexts/ProfilePhotoContext";


const PhotoCard = () => {
    
    
    const { profilePhoto, setProfilePhoto } = useProfilePhoto();
    const [editing, setEditing] = useState(false);
    const [uploaded, setUploaded] = useState(false);
    
    const [newPhoto, setNewPhoto] = useState(null);
    const [newPhotoError, setNewPhotoError] = useState('');

    const handlePhotoDeletion = async () => {
        try{
            const response = await deleteProfilePhoto();
            setProfilePhoto(null);
        }catch(error){
            console.log(error);
        }
    }

    const handleFileUpload = (event) => {
        setNewPhotoError('');
        const file = event.target.files[0];
        
        if (file) {
          const allowedTypes = ["image/jpeg", "image/png", "image/jpg"];
          const maxFileSize = 5 * 1024 * 1024; // 5 MB in bytes
        
          if (!allowedTypes.includes(file.type)) {
            setNewPhotoError('Geçersiz dosya türü. Sadece JPEG, JPG ve PNG dosyalar kabul edilir.');
            return;
          }
          if (file.size > maxFileSize) {
            setNewPhotoError("Dosya boyutu 5MB'den büyük olmamalı.");
            return;
          }
          setNewPhoto(file);
          setUploaded(true);
          console.log('File selected:', file);
        }
    }

    const handleUpdateCancellation = () => {
        setEditing(false);
        setUploaded(false);
        setNewPhoto(null);
        setNewPhotoError('');
    }

    const handlePhotoUpdate = async () => {
        if(newPhoto === null){
            setNewPhotoError('Lütfen dosya seçiniz.');
        }
        try{
            const formData = new FormData();
            formData.append('file', newPhoto);
            const response = await uploadProfilePhoto(formData);
            console.log(response);
            setEditing(false);
            setNewPhoto(null);
            setUploaded(false);
            setProfilePhoto(URL.createObjectURL(newPhoto));
        }catch(error){
            console.log(error);
            setNewPhotoError('Dosya yüklenemedi.');
            setNewPhoto(null);
            setUploaded(false);
        }
    }
    
    return(
        <div className='photo-card'>
            {!editing ? (
                <>
                    <div className='photo-container'>
                        <img src={profilePhoto ? profilePhoto : 'icons/profile-picture.svg'} className="profile-photo" width='128px' height='128px'/>
                    </div>
                    <span className='card-options'>
                        <button className='btn' onClick={() => setEditing(true)}>Değiştir</button>
                        <button className='btn delete-btn' disabled={!profilePhoto} onClick={handlePhotoDeletion}>Sil</button>
                    </span>
                </>
            ) : (
                <>
                    <div className='photo-upload input-container'>
                        {uploaded ? (
                            <Document
                                file={newPhoto}
                                isRemovable={true}
                                onRemove={() => {setNewPhoto(null), setUploaded(false)}}
                            />
                        ) : (
                            <>
                                <input
                                    type="file"
                                    className='input'
                                    accept="image/jpeg,image/png,image/jpg"
                                    onChange={handleFileUpload}
                                />
                                {newPhotoError && <p className='error-message'>{newPhotoError}</p>}
                            </>
                        )}
                    </div>
                    <span className='card-options'>
                        <button className='btn delete-btn' onClick={handleUpdateCancellation}>İptal</button>
                        <button className='btn' onClick={handlePhotoUpdate}>Kaydet</button>
                    </span>    
                </>
            )}
        </div>
    );
}
export default PhotoCard;