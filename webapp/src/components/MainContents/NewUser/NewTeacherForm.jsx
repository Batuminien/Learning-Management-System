import { useEffect, useState } from "react";
import InputField from "../../common/InputField/InputField";
import DateInput from "../../common/DateInput/DateInput";




const NewTeacherForm = () => {


    const [firstName, setFirstName] = useState('');
    const [firstNameError, setFirstNameError] = useState('');

    const [lastName, setLastName] = useState('');
    const [lastNameError, setLastNameError] = useState('');


    useEffect(() => {
        console.log('sınıfları çek');
    }, []);

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
                    value={lastName}
                    onChange={(event) => {setLastName(event.target.value), setLastNameError('')}}
                    errorMessage={lastNameError}
                />
                <DateInput
                    title={'Doğum Tarihi'}
                    onInput={(date) => {}}
                />
                <InputField
                    label={'E-posta Adresi'}
                    value={lastName}
                    onChange={(event) => {setLastName(event.target.value), setLastNameError('')}}
                    errorMessage={lastNameError}
                />
                <InputField
                    label={'Telefon Numarası'}
                    placeholder={'05xx xxx xx xx'}
                    value={lastName}
                    onChange={(event) => {setLastName(event.target.value), setLastNameError('')}}
                    errorMessage={lastNameError}
                />
                

            </div>
        </div>
    );
}
export default NewTeacherForm;