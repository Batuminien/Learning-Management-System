import { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../../../contexts/AuthContext';
import { getClasses } from '../../../../services/classesService';
import ClassesDropdown from '../../../common/ClassesDropdown/ClassesDropdown';
import SubjectsDropdown from '../../../common/SubjectsDropdown/SubjectsDropdown';
import DateInput from '../../../common/DateInput/DateInput';




const OfficerAttendance = () => {

    const { user } = useContext(AuthContext);
    const [requestReload, setReloadRequest] = useState(false);

    const [selectedClass, setSelectedClass] = useState({name : '', id : null});
    const [classError, setClassError] = useState('');

    const [selectedSubject, setSelectedSubject] = useState({name : '', id : null});
    const [subjectError, setSubjectError] = useState('');

    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');


    const handleSearch = () => {
        console.log('aranacak')
    }

    return(
        <>
            <div className='search'>
                <div className='search-options'>
                    <ClassesDropdown
                        user={user}
                        onClassChange={(newClass) => setSelectedClass(newClass)}
                        onError={(error) => setReloadRequest(true)}
                        classErrorMessage={classError}
                    />
                    <SubjectsDropdown
                        user={user}
                        classID={selectedClass.id}
                        onSubjectChange={(newSubject) => setSelectedSubject(newSubject)}
                        onError={(error) => setReloadRequest(true)}
                        subjectErrorMessage={subjectError}
                    />
                    <DateInput
                        title='Başlangıç Tarihi'
                        onInput={(date) => {setStartDate(date)}}
                    />
                    <DateInput
                        title='Bitiş Tarihi'
                        onInput={(date) => {setEndDate(date)}}
                    />
                    <button className='btn' onClick={handleSearch}>Ara</button>
                </div>
            </div>
        </>
    );
}
export default OfficerAttendance;