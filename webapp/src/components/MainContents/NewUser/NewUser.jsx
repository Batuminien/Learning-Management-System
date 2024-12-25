import { useState } from 'react';
import './NewUser.css';
import NavigationOption from '../../common/NavigationOption/NavigationOption';
import NewStudentForm from './NewStudentForm';
import NewTeacherForm from './NewTeacherForm';

const NewUser = () => {

    const [userType, setUserType] = useState('ROLE_STUDENT'); 

    const [creationSuccess, setCreationSuccess] = useState(''); 
    const [creationError, setCreationError] = useState(''); 

    return(
        <div className="new-user">
            <div className="options">
                <NavigationOption 
                    title='Öğrenci'
                    isHighlighted={userType === 'ROLE_STUDENT'}
                    onClick={() => {
                        setUserType('ROLE_STUDENT');
                    }}
                />
                <NavigationOption 
                    title='Öğretmen'
                    isHighlighted={userType === 'ROLE_TEACHER'}
                    onClick={() => {
                        setUserType('ROLE_TEACHER');
                    }}
                />
            </div>
                {userType === 'ROLE_STUDENT'
                    ? (<NewStudentForm 
                            onSubmit={() => {setCreationSuccess(''), setCreationError('')}}
                            onCreationSuccess={(generatedPassword) => {setCreationSuccess(generatedPassword)}}
                            onCreationError={(errorMessage) => {setCreationError(errorMessage)}}
                        />
                    ) : (
                    <NewTeacherForm/>
                )}
            {creationSuccess && (
                <>
                    <p className='register-response' style={{color : 'green'}}>Kayıt Başarılı</p>
                    <p className='register-response' style={{color : 'green'}}>Kullanıcı Şifresi : {creationSuccess}</p>
                </>
            )}
            {creationError && (
                <>
                    <p className='register-response' style={{color : 'red'}}>Kayıt Başarısız</p>
                    <p className='register-response' style={{color : 'red'}}>{creationError}</p>
                </>
            )}
        </div>
    );
}

export default NewUser;