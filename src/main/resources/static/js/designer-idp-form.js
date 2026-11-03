document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("idpForm");
  const notice = document.getElementById("notice");
  const id = new URLSearchParams(location.search).get("id");
  const section = document.getElementById("photoManagement");
  const container = document.getElementById("existingPhotos");
  const photoCount = document.getElementById("photoCount");
  const submit = document.getElementById("submit");
  if (id && !/^\d+$/.test(id)) {
    f1Alert(notice, "Invalid project ID");
    submit.disabled = true;
    return;
  }

  async function loadExistingPhotos() {
    if (!id) return;
    section.hidden = false;
    container.textContent = "Loading project photos...";
    try {
      const photos = await f1Request(`${f1IdpPath(id)}/images`);
      container.replaceChildren();
      photoCount.textContent = `${photos.length} photo(s) uploaded (maximum 10)`;
      if (!photos.length) {
        container.textContent = "No photos uploaded yet.";
        return;
      }
      for (const image of photos) {
        const card = document.createElement("div");
        card.className = "photo-card";
        const img = document.createElement("img");
        img.src = image.imageUrl;
        img.alt = "Project photo";
        const status = document.createElement("div");
        status.className = "photo-tag";
        status.textContent = image.cover ? "★ Cover photo" : "Project photo";
        const cover = document.createElement("button");
        cover.type = "button";
        cover.className = "btn secondary";
        cover.textContent = image.cover ? "Current cover" : "Set as Cover";
        cover.disabled = Boolean(image.cover);
        cover.addEventListener("click", async () => {
          cover.disabled = true;
          try {
            await f1Request(`${f1IdpPath(id)}/images/${image.imageID}/cover`, {method:"PUT"});
            await loadExistingPhotos();
          } catch (e) { f1Alert(notice, e.message); cover.disabled = false; }
        });
        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "btn secondary danger";
        remove.textContent = "Remove Photo";
        remove.addEventListener("click", async () => {
          if (!confirm("Remove this photo from the project?")) return;
          remove.disabled = true;
          try {
            await f1Request(`${f1IdpPath(id)}/images/${image.imageID}`, {method:"DELETE"});
            await loadExistingPhotos();
          } catch (e) { f1Alert(notice, e.message); remove.disabled = false; }
        });
        card.append(img, status, cover, remove);
        container.append(card);
      }
    } catch (error) {
      container.textContent = "Unable to load project photos.";
      f1Alert(notice, error.message);
    }
  }

  async function initialize() {
    try {
      const categories = await f1Request("/api/categories");
      const select = form.elements.namedItem("categoryId");
      select.replaceChildren(new Option("Choose a category", ""));
      for (const category of categories) {
        select.add(new Option(category.categoryName, category.categoryID));
      }
      if (id) {
        document.getElementById("heading").textContent = "Edit Project";
        const project = await f1Request(f1IdpPath(id));
        for (const field of ["title", "categoryId", "propertyType", "budget", "projectStatus", "description"]) {
          const input = form.elements.namedItem(field);
          if (input) input.value = project[field] ?? "";
        }
        // Surface an unexpected status rather than overwriting it with Draft.
        const status = form.elements.namedItem("projectStatus");
        if (status.selectedIndex < 0) {
          f1Alert(notice, `Unknown existing publication status: ${project.projectStatus}. Do not save until corrected.`);
          submit.disabled = true;
        }
        await loadExistingPhotos();
      }
    } catch (error) { f1Alert(notice, error.message); }
  }

  form.addEventListener("submit", async event => {
    event.preventDefault();
    if (!form.reportValidity()) return;
    submit.disabled = true;
    submit.textContent = "Saving...";
    f1Alert(notice, "");
    const payload = {
      title: form.elements.namedItem("title").value.trim(),
      categoryId: Number(form.elements.namedItem("categoryId").value),
      propertyType: form.elements.namedItem("propertyType").value,
      budget: Number(form.elements.namedItem("budget").value),
      projectStatus: form.elements.namedItem("projectStatus").value,
      description: form.elements.namedItem("description").value.trim()
    };
    let saved;
    try {
      saved = await f1Request(id ? f1IdpPath(id) : F1_ENDPOINTS.idps,
        {method: id ? "PUT" : "POST", body: JSON.stringify(payload)});
      const photos = form.elements.namedItem("images").files;
      if (photos.length) {
        const upload = new FormData();
        for (const file of photos) upload.append("images", file);
        const response = await fetch(`${f1IdpPath(id || saved.idpId)}/images`,
          {method:"POST", credentials:"same-origin", body:upload});
        if (!response.ok) {
          const error = await response.json().catch(() => ({}));
          throw new Error(error.message || `Project saved, but photo upload failed (${response.status})`);
        }
      }
      location.href = "designer-idps.html";
    } catch (error) {
      f1Alert(notice, error.message);
      submit.disabled = false;
      submit.textContent = "Save Project";
      if (id) await loadExistingPhotos();
    }
  });
  initialize();
});
