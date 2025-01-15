import { useContext, useState } from "react";
import InputField from "../../common/InputField";
import DateInput from "../../common/DateInput";
import { AuthContext } from "../../../contexts/AuthContext";
import CourseClassMatching from "./CourseClassMatching";
import { generateRandomPassword, isValidEmail, isValidName, isValidPhoneNumber, isValidTC } from "./NewUserUtils";
import authService from "../../../services/authService";
import { addTeacherToCourse } from "../../../services/coursesService";

const NewTeacherForm = ({onSubmit, onCreationSuccess, onCreationError}) => {
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

    const [teacherCourse, setTeacherCourse] = useState({name : '', id : null});
    const [courseError, setCourseError] = useState('');
    const [classError, setClassError] = useState('');
    const [teacherClasses, setTeacherClasses] = useState([]);

    const createTeacher = async () => {
        onSubmit();
        console.log(teacherClasses);
        let hasError = false;
        if(!isValidName(firstName)) {setFirstNameError('Geçersiz ad'), hasError=true}
        if(!isValidName(lastName)) {setLastNameError('Geçersiz soyad'), hasError=true}
        if(!isValidTC(TC)) {setTcError('Geçersiz TC kimlik numarası'), hasError=true}
        if(!isValidEmail(mail)) {setMailError('Geçersiz e-posta adresi'), hasError=true}
        if(!isValidPhoneNumber(phoneNumber)) {setPhoneNumberError('Numara 0 ile başlamalı ve 11 hane olmalıdır')}
        if(birthDate === '') {setBirthDateError('Doğum tarihi seçiniz'), hasError=true}
        if(teacherCourse.name === '') {setCourseError('Lütfen ders seçimi yapınız.'), hasError=true}
        if(teacherClasses.length === 0) {setClassError('Lütfen sınıf seçimi yapınız'), hasError= true}
        if(hasError) return;

        try{
            const registerPayload = {
                firstName : firstName.charAt(0).toUpperCase() + firstName.slice(1),
                lastName : lastName.charAt(0).toUpperCase() + lastName.slice(1),
                email : mail,
                role : 'ROLE_TEACHER',
                schoolLevel : 'HIGH_SCHOOL',
                username : firstName + '.' + lastName,
                password : generateRandomPassword(),
            }
            console.log(registerPayload);
            const response = await authService.register(registerPayload);
            console.log(response);
            try{
                const courseResponse = await addTeacherToCourse(teacherCourse.id, response.data.userId, teacherClasses.map(singleClass => singleClass.value));
                console.log(courseResponse);
                onCreationSuccess(registerPayload.password);

            }catch(error){
                throw error;
            }

        }catch(error){
            console.log(error);
            onCreationError(' ');
        }

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
                onCourseSelection={(course) => {setTeacherCourse(course), setCourseError('')}}
                onClassSelection={(classes) => {setTeacherClasses(classes), setClassError('')}}
                courseError={courseError}
                classError={classError}
            />
            <button type='submit' className='save-btn btn' onClick={createTeacher}>Kaydet</button>
        </div>
    );
}
export default NewTeacherForm;