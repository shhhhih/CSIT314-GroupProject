const detailsForm = document.getElementById("detailsForm");

const usernameInput = document.getElementById("username");
const emailInput = document.getElementById("email");

const editButton = document.getElementById("editButton");
const cancelButton = document.getElementById("cancelButton");
const saveButton = document.getElementById("saveButton");

const detailsMessage = document.getElementById("detailsMessage");

// Store original profile information
let originalProfile = {};

function showMessage(message, type) {
    detailsMessage.textContent = message;
    detailsMessage.className = "alert " + type;
    detailsMessage.hidden = false;
}

function formatDate(dateValue) {

    if (!dateValue) {
        return "—";
    }

    const date = new Date(dateValue);

    if (isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleDateString();
}

function displayProfile(data) {

    // Personal information
    usernameInput.value = data.username || "";
    emailInput.value = data.email || "";

    document.getElementById("accountRole").value =
        data.role || "Customer";

    document.getElementById("accountStatusField").value =
        data.accountStatus || "Unknown";

    document.getElementById("registrationDate").value =
        formatDate(data.createdAt);

    document.getElementById("summaryName").textContent =
        data.username || "Customer";

    document.getElementById("avatar").textContent =
        data.username
            ? data.username.charAt(0).toUpperCase()
            : "U";

    document.getElementById("accountStatus").textContent =
        data.accountStatus || "Unknown";

    document.getElementById("role").textContent =
        data.role || "Customer";

    document.getElementById("memberSince").textContent =
        formatDate(data.createdAt);
}

// LOAD CUSTOMER PROFILE
async function loadProfile() {

    try {

        const response = await fetch("/api/profile");

        // Redirect to login if not logged in
        if (response.status === 401) {
            window.location.href = "/login.html";
            return;
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message || "Unable to load profile."
            );
        }

        // Save original information
        originalProfile = {
            username: data.username,
            email: data.email
        };

        // Display information
        displayProfile(data);

    } catch (error) {

        showMessage(error.message, "error");

        console.error("Profile loading error:", error);
    }
}

function setEditMode(enabled) {

    usernameInput.disabled = !enabled;
    emailInput.disabled = !enabled;

    editButton.hidden = enabled;
    cancelButton.hidden = !enabled;
    saveButton.hidden = !enabled;
}

editButton.addEventListener("click", function () {

    setEditMode(true);

    detailsMessage.hidden = true;
});

cancelButton.addEventListener("click", function () {

    // Restore original information
    usernameInput.value = originalProfile.username;
    emailInput.value = originalProfile.email;

    setEditMode(false);

    detailsMessage.hidden = true;
});

// SAVE PROFILE CHANGES
detailsForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const updatedProfile = {
        username: usernameInput.value.trim(),
        email: emailInput.value.trim()
    };

    try {

        const response = await fetch("/api/profile", {

            method: "PUT",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(updatedProfile)

        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                data.message || "Unable to update profile."
            );
        }

        // Update original information
        originalProfile = {
            username: data.username,
            email: data.email
        };

        // Refresh displayed information
        displayProfile(data);

        // Exit edit mode
        setEditMode(false);

        showMessage(
            "Profile updated successfully!",
            "success"
        );

    } catch (error) {

        showMessage(error.message, "error");

        console.error("Profile update error:", error);
    }

});

loadProfile();