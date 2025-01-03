import { useContext, useEffect, useState } from "react";
import { getClasses } from "../../../../services/classesService";
import { AuthContext } from "../../../../contexts/AuthContext";

import Loading from '../../../common/Loading/Loading';
import Warning from '../../../common/IconComponents/Warning';

import React from 'react';
import Select from 'react-select';
import makeAnimated from 'react-select/animated';
import { createAnnouncement } from "../../../../services/announcementService";

const animatedComponents = makeAnimated();

const NewAnnouncement = () => {
    const { user } = useContext(AuthContext);
    const [loading, setLoading] = useState(false);
    const [loadingError, setLoadingError] = useState(false);

    const [creationSuccess, setCreationSucces] = useState(false);
    const [creationError, setCreationError] = useState(false);

    const [classes, setClasses] = useState([]);
    const [selectedClasses, setSelectedClasses] = useState([]);
    const [classError, setClassError] = useState('');

    const [announcementNote, setAnnouncementNote] = useState('');
    const [announcementNoteError, setAnnouncementNoteError] = useState('');

    const [announcementTitle, setAnnouncementTitle] = useState('');
    const [announcementTitleError, setAnnounCementTitleError] = useState('');

    useEffect(() => {
        const fetchClasses = async () => {
            try{
                setLoading(true);
                const response = await getClasses(user.role, user.accessToken);
                const allClasses = response.data.map(singleClass => ({label : singleClass.name, value : Number(singleClass.id)}));
                setClasses(allClasses);
            }catch(error){
                setLoadingError(true);
            }finally{
                setLoading(false);
            }
        }
        fetchClasses();
    }, []);

    const handleSubmit = async () => {
        setCreationSucces(false);
        setCreationError(false);

        let hasError = false;
        if(selectedClasses.length === 0) {setClassError('En az bir sınıf seçilmelidir.'), hasError=true}
        if(announcementTitle.length === 0) {setAnnounCementTitleError('Lütfen duyuru başlığı giriniz.'), hasError=true}
        if(announcementNote.length === 0) {setAnnouncementNoteError('Lütfen duyuru notu giriniz.'), hasError=true}

        if(hasError) return;
        const announcementData = {
            title : announcementTitle,
            content : announcementNote,
            classIds : selectedClasses.map(cls => cls.value),
        }
        try{
            const response = await createAnnouncement(announcementData, user.accessToken);
            setCreationSucces(true);
        }catch(error){
            console.log(error);
            setCreationError(true);
        }
    }

    if(loadingError) return <Warning/>

    return(
        <>
            {loading ? (
                <Loading/>
            ) : (
                <div className="newAssignmentForm">
                    <div className="input-container">
                        <label className="label">Duyuru Sınıfları</label>
                        <Select
                            className="input"
                            closeMenuOnSelect={false}
                            isMulti
                            options={classes}
                            value={selectedClasses}
                            onChange={(selected) => {
                                setSelectedClasses(selected);
                                setClassError('');
                            }}
                            placeholder={'Sınıfları Seçiniz'}
                            components={animatedComponents}
                        />
                        {classError && <p className="error-message">{classError}</p>}
                    </div>

                    <div className="input-container">
                        <label className="label">Duyuru Başlığı</label>
                        <input
                            type="text"
                            className="input"
                            value={announcementTitle}
                            onChange={(event) => {setAnnouncementTitle(event.target.value), setAnnounCementTitleError('')}}
                        />
                        {announcementTitleError && <p className="error-message">{announcementTitleError}</p>}
                    </div>

                    <div className="input-container">
                        <label className="label">Duyuru Notu</label>
                        <textarea
                            className="input"
                            value={announcementNote}
                            onChange={(event) => {setAnnouncementNote(event.target.value), setAnnouncementNoteError('')}}
                            style={{resize : 'none', width : '100%', aspectRatio : 5}}
                            lang='tr'
                            spellCheck='false'
                        />
                        {announcementNoteError && <p className="error-message">{announcementNoteError}</p>}
                    </div>
                    <button className="btn save-btn" onClick={handleSubmit}>Oluştur</button>
                    {creationSuccess && <p className="register-response" style={{color : 'green'}}>Duyuru başarıyla oluşturuldu.</p>}
                    {creationError && <p className="register-response" style={{color : 'red'}}>Duyuru oluşturulurken hata!</p>}
                </div>
            )}
        </>
    );
}
export default NewAnnouncement;