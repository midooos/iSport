
function Button(props) {

    let variant = props.variant;
    let type = props.type;

    if (variant === undefined) {
        variant = "primary";
    }

    if (type === undefined) {
        type = "button";
    }

    return (
        <button
            type={type}
            className={"btn btn--" + variant}
            onClick={props.onClick}
            disabled={props.disabled}
        >
            {props.children}
        </button>
    );
}

export default Button;

