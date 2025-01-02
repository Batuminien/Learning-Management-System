import { useContext, useEffect, useState } from "react";
import { getTimeFromDate } from "../../../utils/dateUtils";
import { ArrowDown, ArrowUp, EditIcon } from "../../../../public/icons/Icons";
import { ClosedEnvelopeIcon, OpenEnvelopeIcon } from "../../../../public/icons/Icons";
import { AuthContext } from "../../../contexts/AuthContext";
import { deleteAnnouncement, updateAnnouncement } from "../../../services/announcementService";



const SingleAnnouncement = ({announcement, onRead = null, onUnread}) => {
    const { user } = useContext(AuthContext);
    const [isOwnAnnouncement, setIsOwnAnnouncement] = useState(false);
    const [canModify, setCanModify] = useState(false);

    const [editing, setEditing] = useState(false);
    const [newContent, setNewContent] = useState(announcement.content);

    const [deletionSuccess, setDeletionSuccess] = useState(false);
    const [announcementError, setAnnouncementError] = useState('');
    const [updateSuccess, setUpdateSuccess] = useState(false);

    useEffect(() => {
        console.log(announcement);
        const ownAnnouncement = announcement.createdById == user.id;
        if(ownAnnouncement) setIsOwnAnnouncement(true);
        if(user.role === 'ROLE_ADMIN' || user.role === 'ROLE_COORDINATOR') setCanModify(true);
        else if(user.role === 'ROLE_TEACHER' && ownAnnouncement) setCanModify(true);    
    }, []);

    const [isExpanded, setIsExpanded] = useState(false);

    const handleAnnouncementUpdate = async () => {
        try{
            setAnnouncementError('');
            setUpdateSuccess(false);
            const newAnnouncement = {...announcement, content : newContent};
            const response = await updateAnnouncement(announcement.id, newAnnouncement, user.accessToken);
            announcement.content = newContent
            setUpdateSuccess(true);
            setEditing(false);
        }catch(error){
            setAnnouncementError('Duyuru güncellenemedi!');
        }
    }

    const handleAnnouncementDeletion = async () => {
        try{
            const response = await deleteAnnouncement(announcement.id, user.accessToken);
            setDeletionSuccess(true);
        }catch(error){
            console.log(error);
            setAnnouncementError('Duyuru kaldırılırken hata!');
        }
    }

    if(deletionSuccess) {return(<div className="unit-container success register-response">Duyuru başarıyla silindi.</div>);}

    return(
        <div className="unit-container" style={{backgroundColor : announcement.read ? 'var(--read-announcement-background)' : ''}}>
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className="unit-subject">{announcement.title}</span>
                    <span className="unit-subject">{announcement.createdByName} |</span>
                    <span className="unit-dueDate">{getTimeFromDate(announcement.createdAt)} - {new Date(announcement.createdAt).toLocaleDateString('en-GB')}</span>
                </div>
                <div style={{display : 'flex', alignItems : 'center',  columnGap : '6px'}}>
                    <div style={{display : 'grid', placeItems : 'center'}}
                        onClick={(event) => {
                            event.stopPropagation();
                            if (!announcement.read) {
                                onRead(announcement.id);
                            }
                            else{
                                onUnread(announcement.id);
                            }
                        }}
                    >
                        {!isOwnAnnouncement && (announcement.read ? <OpenEnvelopeIcon/> : <ClosedEnvelopeIcon />)}
                    </div>
                    <button className="expand-btn">
                        {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                    </button>
                </div>
            </div>
            {isExpanded && (
                <>
                <div className="unit-body">
                    <div style={{border : '1px solid grey',}}></div>
                    
                    <div className="unit-body-section">
                        <span className="unit-section-title">Açıklama : </span>
                        {editing
                            ? (
                                <textarea
                                    className='input text-area'
                                    lang="tr"
                                    spellCheck={false}
                                    defaultValue={announcement.content}
                                    onChange={(event) => setNewContent(event.target.value)}
                                >

                                </textarea>
                            )
                            : <span className="unit-section-text">{announcement.content}</span>
                        }
                    </div>
                    {canModify && (
                        <div className='announcement-options'>
                            {editing
                                ? (
                                    <>
                                        <button className="btn delete-btn" onClick={() => setEditing(false)}>İptal</button>
                                        <button className="btn" onClick={handleAnnouncementUpdate}>Kaydet</button>
                                    </>
                                ) : (
                                    <>
                                        <div onClick={() => setEditing(true)}>
                                            <EditIcon />
                                        </div>
                                        <button className="delete-btn btn" onClick={handleAnnouncementDeletion}>Sil</button>
                                    </>
                                )
                            }
                        </div>
                    )}
                </div>
                {announcementError && <p className="register-response failure">{announcementError}</p>}
                {updateSuccess && <p className="register-response success">Duyuru başarıyla güncellendi.</p>}
                </>
            )}
        </div>
    );
}
export default SingleAnnouncement;