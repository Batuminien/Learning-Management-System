import { useContext, useState } from "react";
import InputField from "../../common/InputField";
import DateInput from "../../common/DateInput";
import { AuthContext } from "../../../contexts/AuthContext";
import CourseClassMatching from "./CourseClassMatching";
import { isValidEmail, isValidName, isValidPhoneNumber, isValidTC } from "./NewUserUtils";




const NewTeacherForm = ({onSubmit, onCreationsuccess, onCreationError}) => {
    const { user } = useContext(AuthContext);

    const [firstName, setFirstName] = useState('');
    const [firstNameError, setFirstNameError] = useState('');

    const [lastName, setLastName] = useState('');
    const [lastNameError, setLastNameError] = useState('');

    const [TC, setTC] = useState('');
    const [TcError, setTcError] = useState('');

    const [birthDate, setBirthDate] = useState('');
    const [birthDateError, setBirthDateError] = useState('');

    const [mail, setMail] = useState('');
    const [mailError, setMailError] = useState('');

    const [phoneNumber, setPhoneNumber] = useState('');
    const [phoneNumberError, setPhoneNumberError] = useState('');

    const [teacherCourse, setTeacherCourse] = useState({});
    const [courseError, setCourseError] = useState('');
    const [teacherClasses, setTeacherClasses] = useState([]);

    const handleClassSelection = (course, selections) => {
        console.log('the course is : ', course);
        console.log('selected classes : ', selections);
        setTeacherCourse(course);
        setTeacherClasses(selections);
    }

    const createTeacher = async () => {
        onSubmit();
        let hasError = false;
        if(!isValidName(firstName)) {setFirstNameError('Geçersiz ad'), hasError=true}
        if(!isValidName(lastName)) {setLastNameError('Geçersiz soyad'), hasError=true}
        if(!isValidTC(TC)) {setTcError('Geçersiz TC kimlik numarası'), hasError=true}
        if(!isValidEmail(mail)) {setMailError('Geçersiz e-posta adresi'), hasError=true}
        if(!isValidPhoneNumber(phoneNumber)) {setPhoneNumberError('Numara 0 ile başlamalı ve 11 hane olmalıdır')}
        if(birthDate === '') {setBirthDateError('Doğum tarihi seçiniz'), hasError=true}
        if(teacherCourse.name === '') {setCourseError('Lütfen ders ve sınıf seçimi yapınız.'), hasError=true}
        if(hasError) return;
    }

    return(
        <div className="form">
            <div className="form-title">Yönetici</div>
            <div className="input-fields">
                <InputField
                    label={'Adı'}
                    value={firstName}
                    onChange={(event) => {setFirstName(event.target.value), setFirstNameError('')}}
                    errorMessage={firstNameError}
                />
                <InputField
                    label={'Soyadı'}
                    value={lastName}
                    onChange={(event) => {setLastName(event.target.value), setLastNameError('')}}
                    errorMessage={lastNameError}
                />
                <InputField
                    label={'TC Kimlik Numarası'}
                    value={TC}
                    onChange={(event) => {setTC(event.target.value), setTcError('')}}
                    errorMessage={TcError}
                />
                <DateInput
                    title={'Doğum Tarihi'}
                    onInput={(date) => {setBirthDate(date), setBirthDateError('')}}
                    errorMessage={birthDateError}
                />
                <InputField
                    label={'E-posta Adresi'}
                    value={mail}
                    onChange={(event) => {setMail(event.target.value), setMailError('')}}
                    errorMessage={mailError}
                />
                <InputField
                    label={'Telefon Numarası'}
                    placeholder={'05xx xxx xx xx'}
                    value={phoneNumber}
                    onChange={(event) => {setPhoneNumber(event.target.value), setPhoneNumberError('')}}
                    errorMessage={phoneNumberError}
                />
            </div>
            <div className="form-title">Dersler</div>
            <CourseClassMatching
                onSelection={(course ,selections) => handleClassSelection(course, selections)}
                courseError={courseError}
            />
            <button type='submit' className='save-btn btn' onClick={createTeacher}>Kaydet</button>
        </div>
    );
}
export default NewTeacherForm;