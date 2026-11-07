
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    const message = document.getElementById("message");
    const submitButton = form.querySelector('button[type="submit"]');

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = form.elements.namedItem("username").value.trim();
        const email = form.elements.namedItem("email").value.trim();
        const password = form.elements.namedItem("password").value;
        const confirmPassword = form.elements.namedItem("confirmPassword").value;

        message.hidden = true;
        message.textContent = "";
        message.className = "alert";

        if (password !== confirmPassword) {
            message.textContent = "Passwords do not match.";
            message.classList.add("error");
            message.hidden = false;
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "Creating Account...";

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username,
                    email,
                    password,
                    confirmPassword
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Registration failed.");
            }

            message.textContent =
                data.message || "Account created successfully! Please log in.";
            message.classList.add("success");
            message.hidden = false;

            form.reset();

        } catch (error) {
            message.textContent = error.message || "Unable to register.";
            message.classList.add("error");
            message.hidden = false;

        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "Create Account";
        }
    });
});
