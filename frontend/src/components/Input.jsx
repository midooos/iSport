
function Input(props) {

    let champ;

    if (props.as === "select") {
        champ = (
            <select
                id={props.id}
                name={props.name}
                value={props.value}
                onChange={props.onChange}
            >
                {props.children}
            </select>
        );
    } 
    else {
        champ = (
            <input
                id={props.id}
                type={props.type}
                name={props.name}
                value={props.value}
                placeholder={props.placeholder}
                onChange={props.onChange}
            />
        );
    }

    return (
        <div className="field">

            <label htmlFor={props.id}>
                {props.label}
            </label>

            {champ}

            {props.hint && (
                <p className="field__hint">
                    {props.hint}
                </p>
            )}

            {props.error && (
                <p className="field__error" role="alert">
                    {props.error}
                </p>
            )}

        </div>
    );
}

export default Input;

