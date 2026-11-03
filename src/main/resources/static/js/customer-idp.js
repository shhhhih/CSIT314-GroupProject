
document.addEventListener("DOMContentLoaded", async () => {

    const id = new URLSearchParams(
        window.location.search
    ).get("id");

    const saveBtn = document.getElementById("saveBtn");
    const removeBtn = document.getElementById("removeBtn");

    const photo = document.getElementById("mainPhoto");
    const thumbs = document.getElementById("thumbnails");

    // Update text on the page
    function set(name, value) {
        const element = document.getElementById(name);

        if (element) {
            element.textContent = value ?? "—";
        }
    }

    if (!id || !/^\d+$/.test(id)) {
        set("title", "Invalid project ID");
        return;
    }

    // API Requests
    async function request(url, options = {}) {

        const response = await fetch(url, {
            credentials: "same-origin",
            ...options
        });

        const data = await response.json()
            .catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                data.message ||
                `Request failed (${response.status})`
            );
        }

        return data;
    }

    // Favourite button state
    function setSaved(saved) {
        if (saveBtn) saveBtn.hidden = saved;
        if (removeBtn) removeBtn.hidden = !saved;
    }

    // Check existing favourites
    async function refreshSaved() {

        const response = await fetch("/api/favourites", {
            credentials: "same-origin"
        });

        if (!response.ok) return;

        const favourites = await response.json();

        const alreadySaved = favourites.some(item =>
            String(item.idp?.idpId) === String(id)
        );

        setSaved(alreadySaved);
    }

    // Project Photo Gallery
    function displayGallery(images, projectTitle) {

        const gallery = (Array.isArray(images) ? images : [])
            .map(image => {
                if (typeof image === "string") return image;
                return image?.imageUrl;
            })
            .filter(Boolean);

        if (!photo || !thumbs) return;

        photo.replaceChildren();
        thumbs.replaceChildren();

        if (gallery.length === 0) {
            photo.textContent = "No photos uploaded yet";
            return;
        }

        let currentIndex = 0;
        let touchStartX = null;

        const selected = document.createElement("img");

        selected.alt = projectTitle || "Project photo";
        selected.style.cssText =
            "width:100%;height:100%;object-fit:cover;" +
            "border-radius:12px;display:block;";

        const previous = document.createElement("button");
        const next = document.createElement("button");

        const arrowStyle =
            "position:absolute;top:50%;" +
            "transform:translateY(-50%);" +
            "width:42px;height:42px;border:0;" +
            "border-radius:50%;background:rgba(0,0,0,.6);" +
            "color:white;font-size:20px;cursor:pointer;" +
            "z-index:2;";

        previous.type = "button";
        previous.textContent = "❮";
        previous.setAttribute("aria-label", "Previous photo");
        previous.style.cssText = arrowStyle + "left:12px;";

        next.type = "button";
        next.textContent = "❯";
        next.setAttribute("aria-label", "Next photo");
        next.style.cssText = arrowStyle + "right:12px;";

        if (gallery.length < 2) {
            previous.hidden = true;
            next.hidden = true;
        }

        const counter = document.createElement("span");

        counter.setAttribute("aria-live", "polite");
        counter.style.cssText =
            "position:absolute;bottom:12px;right:12px;" +
            "background:rgba(0,0,0,.6);color:white;" +
            "padding:6px 12px;border-radius:20px;" +
            "font-size:13px;z-index:2;";

        photo.style.position = "relative";
        photo.style.overflow = "hidden";
        photo.style.touchAction = "pan-y";

        photo.append(selected, previous, next, counter);

        const thumbnailImages = [];

        gallery.forEach((url, index) => {

            const thumbnail = document.createElement("img");

            thumbnail.src = url;
            thumbnail.alt = `Project photo ${index + 1}`;

            thumbnail.addEventListener("click", () => {
                showImage(index);
            });

            thumbnailImages.push(thumbnail);
            thumbs.appendChild(thumbnail);
        });

        function showImage(index) {

            currentIndex =
                (index + gallery.length) % gallery.length;

            selected.src = gallery[currentIndex];

            counter.textContent =
                `${currentIndex + 1} / ${gallery.length}`;

            thumbnailImages.forEach((image, i) => {
                image.classList.toggle(
                    "current",
                    i === currentIndex
                );
            });
        }

        previous.addEventListener("click", () => {
            showImage(currentIndex - 1);
        });

        next.addEventListener("click", () => {
            showImage(currentIndex + 1);
        });

        photo.addEventListener("touchstart", event => {
            touchStartX = event.changedTouches[0].screenX;
        }, { passive: true });

        photo.addEventListener("touchend", event => {

            if (touchStartX === null) return;

            const difference =
                event.changedTouches[0].screenX - touchStartX;

            if (Math.abs(difference) > 50) {
                showImage(
                    currentIndex + (difference < 0 ? 1 : -1)
                );
            }

            touchStartX = null;
        }, { passive: true });

        showImage(0);
    }

    // Company Logo
    function displayCompanyLogo(logoPath) {

        const companyLogo =
            document.getElementById("companyLogo");

        if (!companyLogo) return;

        if (!logoPath) {
            companyLogo.hidden = true;
            companyLogo.removeAttribute("src");
            return;
        }

        // Normalise the path saved by Spring Boot
        const normalized = String(logoPath)
            .replace(/\\/g, "/")
            .replace(/^\/+/, "");

        // Only display uploaded company logos
        if (!normalized.startsWith("uploads/company-logos/")) {
            companyLogo.hidden = true;
            return;
        }

        companyLogo.onerror = () => {
            companyLogo.hidden = true;
            console.error(
                "Unable to load company logo:",
                companyLogo.src
            );
        };

        companyLogo.src = "/" + normalized;
        companyLogo.hidden = false;
    }

    // Load Project Details
    try {

        const project = await request(
            "/api/idps/" + encodeURIComponent(id)
        );

        set("title", project.title);
        set("category", project.categoryName);
        set("detailCategory", project.categoryName);

        set(
            "byline",
            "By " + (
                project.companyName ||
                "Interior design studio"
            )
        );

        set("description", project.description);
        set("propertyType", project.propertyType);
        set("projectStatus", project.projectStatus);

        set(
            "budget",
            "S$" + Number(
                project.budget || 0
            ).toLocaleString("en-SG")
        );

        set("viewCount", project.viewCount);
        set("shortlistCount", project.shortlistCount);

        set(
            "updatedAt",
            project.updatedAt
                ? new Date(project.updatedAt).toLocaleString("en-SG")
                : "—"
        );

        set("companyName", project.companyName);

        // Display project pictures
        displayGallery(
            project.images,
            project.title
        );

        // Load Designer Company Details
        if (project.companyId) {

            try {

                const company = await request(
                    "/api/companies/" +
                    encodeURIComponent(project.companyId)
                );

                set("companyName", company.companyName);
                set("companyDescription", company.description);
                set("companyAddress", company.address);

                // Display company logo
                displayCompanyLogo(company.logoPath);

                // Email
                const email = document.getElementById(
                    "companyEmail"
                );

                if (email) {
                    email.textContent = company.email || "—";

                    if (company.email) {
                        email.href = "mailto:" + company.email;
                    } else {
                        email.removeAttribute("href");
                    }
                }

                // Phone
                const phone = document.getElementById(
                    "companyPhone"
                );

                if (phone) {
                    phone.textContent = company.phone || "—";

                    if (company.phone) {
                        phone.href = "tel:" + company.phone;
                    } else {
                        phone.removeAttribute("href");
                    }
                }

                // Website
                const website = document.getElementById(
                    "companyWebsite"
                );

                if (website) {

                    if (
                        company.website &&
                        /^https?:\/\//i.test(company.website)
                    ) {
                        website.textContent = company.website;
                        website.href = company.website;
                    } else {
                        website.textContent = "—";
                        website.removeAttribute("href");
                    }
                }

                set("companyLicence", "Not provided");

            } catch (error) {

                console.warn(
                    "Unable to load company details",
                    error
                );
            }
        }

        await refreshSaved();

    } catch (error) {

        console.error(error);
        set("title", error.message);

        if (saveBtn) saveBtn.disabled = true;

        return;
    }

    // Save to Favourites
    if (saveBtn) {
        saveBtn.addEventListener("click", async () => {

            saveBtn.disabled = true;

            try {

                await request("/api/favourites", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        idpId: Number(id)
                    })
                });

                setSaved(true);

                const countElement =
                    document.getElementById("shortlistCount");

                if (countElement) {
                    set(
                        "shortlistCount",
                        Number(countElement.textContent) + 1
                    );
                }

            } catch (error) {

                if (
                    /409|already saved/i.test(error.message)
                ) {
                    setSaved(true);
                } else {
                    alert(error.message);
                }

            } finally {
                saveBtn.disabled = false;
            }
        });
    }

    // Remove from Favourites
    if (removeBtn) {
        removeBtn.addEventListener("click", async () => {

            removeBtn.disabled = true;

            try {

                const response = await fetch(
                    "/api/favourites/" + encodeURIComponent(id),
                    {
                        method: "DELETE",
                        credentials: "same-origin"
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "Unable to remove favourite"
                    );
                }

                setSaved(false);

                const countElement =
                    document.getElementById("shortlistCount");

                if (countElement) {
                    set(
                        "shortlistCount",
                        Math.max(
                            0,
                            Number(countElement.textContent) - 1
                        )
                    );
                }

            } catch (error) {
                alert(error.message);
            } finally {
                removeBtn.disabled = false;
            }
        });
    }

});
