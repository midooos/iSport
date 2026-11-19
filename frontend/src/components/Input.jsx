export default function Input({
  label,
  id,
  error,
  hint,
  as = "input",
  children,
  ...rest
}) {
  const hintId = hint ? `${id}-hint` : undefined;
  const errorId = error ? `${id}-error` : undefined;
  const describedBy = [hintId, errorId].filter(Boolean).join(" ") || undefined;

  const common = {
    id,
    "aria-invalid": error ? "true" : undefined,
    "aria-describedby": describedBy,
    ...rest,
  };

  return (
    <div className={`field${error ? " field--error" : ""}`}>
      <label htmlFor={id}>{label}</label>
      {as === "select" ? (
        <select {...common}>{children}</select>
      ) : (
        <input {...common} />
      )}
      {hint && (
        <p id={hintId} className="field__hint">
          {hint}
        </p>
      )}
      {error && (
        <p id={errorId} className="field__error" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
