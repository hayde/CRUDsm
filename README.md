

### 1. **`.actionInit( () => {} )`**
- **Purpose**: Initialize the CRUD system with initial data.
- **Called When**: `event_init()` is triggered (e.g., on page load).
- **Return**: `Array` of initial objects to populate the list.
- **Example**:
  ```javascript
  crud.actionInit(() => {
    return fetch("/api/items").then(res => res.json()); // Return initial data array
  });
  ```

---

### 2. **`.actionSelected( (selectedID) => {} )`**
- **Purpose**: Handle selection of an item from the list.
- **Called When**: An item is selected (e.g., from a dropdown/list).
- **Parameter**: `selectedID` (current selected tuple ID, not the Object itself.).
- **Return**: Newly selected tuple ID, not the Object.
- **Example**:
  ```javascript
  crud.actionSelected((selectedID) => {
    return document.getElementById("dropdown").value; // Return new selected ID
  });
  ```

---

### 3. **`.actionNew( () => {} )`**
- **Purpose**: Create a new empty object for adding a record.
- **Called When**: `event_new()` is triggered (e.g., "New" button click).
- **Return**: A new empty object (e.g., `{ name: "", age: 0 }`).
- **Example**:
  ```javascript
  crud.actionNew(() => ({ id: null, title: "", content: "" }));
  ```

---

### 4. **`.actionSave( (obj, id) => {} )`**
- **Purpose**: Save/update an object (e.g., send to a server).
- **Called When**: Saving changes (`event_save()` or auto-save).
- **Parameters**:
  - `obj`: The modified object to save.
  - `id`: The ID of the object (null if new).
- **Return**: The saved object (with updated fields like server-generated IDs).
- **Example**:
  ```javascript
  crud.actionSave((obj, id) => {
    return fetch(`/api/items/${id}`, { method: "POST", body: JSON.stringify(obj) });
  });
  ```

---

### 5. **`.actionDelete( (obj) => {} )`**
- **Purpose**: Delete an object.
- **Called When**: `event_delete()` is triggered.
- **Parameter**: `obj` - The object to delete.
- **Return**: `true` if successful, `false` to cancel deletion.
- **Example**:
  ```javascript
  crud.actionDelete((obj) => {
    return confirm("Delete this item?") && fetch(`/api/items/${obj.id}`, { method: "DELETE" });
  });
  ```

---

### 6. **`.actionChanged( () => {} )`**
- **Purpose**: React to form field changes (e.g., enable "Save" button).
- **Called When**: Any registered form field changes.
- **No Return Value**.
- **Example**:
  ```javascript
  crud.actionChanged(() => {
    document.getElementById("save-btn").disabled = false;
  });
  ```

---

### 7. **`.actionValidate( (obj) => {} )`**
- **Purpose**: Validate data before saving.
- **Called When**: Before saving (auto-save or manual save).
- **Parameter**: `obj` - The object to validate.
- **Return**: Validated object (or `null`/`false` to block saving).
- **Example**:
  ```javascript
  crud.actionValidate((obj) => {
    if (!obj.title) alert("Title is required!");
    return obj.title ? obj : null;
  });
  ```

---

### 8. **`.actionRefreshView( (obj) => {} )`**
- **Purpose**: Update the UI with the current object (new/edited/selected).
- **Called When**: The UI needs refreshing (e.g., after save/delete/select).
- **Parameter**: `obj` - The object to display (or `null` for empty form).
- **Example**:
  ```javascript
  crud.actionRefreshView((obj) => {
    document.getElementById("title-field").value = obj?.title || "";
  });
  ```

---

### 9. **`.actionRemovedOrAdded( (lastActionID) => {} )`**
- **Purpose**: Update the list UI after an item is added/removed.
- **Called When**: An item is added/deleted.
- **Parameter**: `lastActionID` - ID of the affected tuple.
- **Example**:
  ```javascript
  crud.actionRemovedOrAdded((id) => {
    document.getElementById(`item-${id}`)?.remove(); // Update the list
  });
  ```

---

### 10. **`.actionInitSearch( () => {} )`**
- **Purpose**: Reset the UI for a new search (e.g., clear search fields).
- **Called When**: `event_initSearch()` is triggered.
- **Example**:
  ```javascript
  crud.actionInitSearch(() => {
    document.getElementById("search-input").value = "";
  });
  ```

---

### 11. **`.actionSearch( () => {} )`**
- **Purpose**: Execute a search and return results.
- **Called When**: `event_search()` is triggered.
- **Return**: `Array` of search results.
- **Example**:
  ```javascript
  crud.actionSearch(() => {
    const query = document.getElementById("search-input").value;
    return fetch(`/api/search?q=${query}`).then(res => res.json());
  });
  ```

---

### Usage Flow:
1. **Initialize**:
   ```javascript
   const crud = new CRUDsm("myForm");
   crud.actionInit(/* ... */).actionSelected(/* ... */) // Set all actions
   crud.initialize(); // Finalize setup
   crud.event_init(); // Load initial data
   ```
2. **Bind UI Events**:
   ```javascript
   document.getElementById("btn-save").addEventListener("click", () => crud.event_save());
   document.getElementById("btn-new").addEventListener("click", () => crud.event_new());
   ```
