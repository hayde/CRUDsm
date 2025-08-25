"use strict";

/***********
 * CRUDsm.js 
 * 
 * actions to be set:
 * .actionInit( () => {} )
 * .actionSelected( () => {} )
 * .actionNew( () => {} )
 * .actionSave( () => {} )
 * .actionDelete( () => {} )
 * .actionChanged( () => {} )
 * .actionValidate( () => {} )
 * .actionRefreshView( () => {} )
 * .actionRemovedOrAdded( () => {} )
 * .actionInitSearch( () => {} )
 * .actionSearch( () => {} )
 * 
 */
class CRUDTuple { #id; #obj; constructor(id, obj) { this.#id = id; this.#obj = obj; } get id() { return this.#id; } get obj() { return this.#obj; } set obj(new_obj) { this.#obj = new_obj; } }
class CRUDTuples {
    #tuples = [];
    #counter = 0;
    #current = -1;
    #last_action = -1;
    #last_object_id = -1;
    #selected = null;

    constructor() {
    }
    add(obj) {
        this.#current = ++this.#counter
        this.#tuples.push(new CRUDTuple(this.#current, obj));

        // mark as "add"
        this.#last_action = +1;
        this.#last_object_id = this.#current;
        return this;
    }
    get(id) {
        return this.#tuples.find((x) => { return x.id == id; });
    }
    change(id, value) {
        var tuple = this.get(id);
        tuple.obj = value;
        this.#last_action = 0;
        this.#last_object_id = -1;
        return this;
    }
    delete(id) {
        this.#tuples = this.#tuples.filter((x) => { return x.id !== id; });
        if (id === this.#current) {
            this.#current = -1;
        }
        // mark as "delete"
        this.#last_action = -1;
        this.#last_object_id = id;
        return this;
    }
    isDeleted() {
        return this.#last_action === -1;
    }
    isAdded() {
        return this.#last_action === +1;
    }
    getLastActionID() {
        return this.#last_object_id;
    }
    getCurrentId() {
        return this.#current;
    }
    get current() {
        return this.get(this.#current);
    }
    set current(id) {
        if (typeof id === CRUDTuple) {
            // we store the id only
            id = id.id;
        }
        if (id <= -1) {
            id = null;
        }
        this.#current = id;
    }
    get selected() {
        return this.#selected;
    }
    set selected(tupleID) {
        this.#selected = tupleID;
    }
    get tuples() {
        return this.#tuples;
    }
    selectFirst() {
        if( this.#tuples ) {
            this.#selected = this.#tuples[0].id;
        }
    }
}
class CRUDsm {

    static #StateMachine = {
        INITIALIZE: Symbol("INITIALIZE"),
        NOTHING_SELECTED: Symbol("NOTHING_SELECTED"),
        PRESELECTED: Symbol("PRESELECTED"),
        SELECTED: Symbol("SELECTED"),
        NEW: Symbol("NEW"),
        CHANGED: Symbol("CHANGED"),
        SAVE: Symbol("SAVE"),
        AUTOMATICSAVE: Symbol("AUTOMATICSAVE"),
        DELETE: Symbol("DELETE"),
        REFRESHVIEW: Symbol("REFRESHVIEW"),
        VALIDATING: Symbol("VALIDATING"),
        SELECTIONLISTCHANGED: Symbol("SELECTIONLISTCHANGED"),
        INIT_SEARCH: Symbol("INIT_SEARCH"),
        SEARCH: Symbol("SEARCH")
    }

    static #CRUDEvents = {
        INITIALIZE: Symbol("INITIALIZE"),
        NEW: Symbol("NEW"),
        SELECT: Symbol("SELECT"),
        SAVE: Symbol("SAVE"),
        DELETE: Symbol("DELETE"),
        CHANGED: Symbol("CHANGED"),
        CLOSING: Symbol("CLOSING"),
        INIT_SEARCH: Symbol("INIT_SEARCH"),
        SEARCH: Symbol("SEARCH"),
        VALIDATE: Symbol("VALIDATE")
    }

    // flags
    #initialized = false;   // the initialise for the complete class
    #initialized_by_event = false;
    #flag_select_initial_pos = -1;
    #changeFlag = false;
    #isInSearch = false;
    #is_in_refresh = false;

    #action_functions = {};
    #selectedTupleID = null;
    #new_object = null;
    #tuples = null;
    #automatic_save = true;     // true by default
    #form_id = null;            // html element id of the form
    #form = null;
    #form_elements = [];
    self = null;
    #form_exclude = [];

    // elements 
    constructor(form_id) {
        // the elements do have a naming convention
        // form_id is the <form id="{form-id}" ...
        // all other elements like input, select, textarea, button, datalist, option, optgroup, ... must 
        // follow the naming convention {form-id}.{field-id}
        // the the crudsm element will be able to have nested forms in forms

        this.#form_id = form_id;

        this.payload = {}; // payload for the user to store process data into it :)
        
        self = this;
    }

    get automaticSave() {
        return this.#automatic_save;
    }
    set automaticSave(value) {
        this.#automatic_save = value;
    }

    selectFirst() {
        if (this.#initialized_by_event) {
            // we need to fire the selection event
            this.#tuples.selectFirst();
            this.event_select();
        } else {
            // if the initialization has not been done by the event, do nothing
            this.#flag_select_initial_pos = 0;
        }
    }

    excludeElements(arr) {
        this.#form_exclude = arr;
    }
    registerElements() {
        var returnValue = false;
        // get the form
        this.#form = document.getElementById(this.#form_id);
        if (this.#form_id && !this.#form) {
            console.error("form unknown in this document: " + form_id);
        } else if (!this.#form_id) {
            // we have not specified a form_id, so nothing to do
            this.#form_elements = [];
            returnValue = true;
        } else {
            this.#form_elements = [];
            // read all elements of the form_elements list
            var tag_names = ["input", "textarea", "select"];
            tag_names.map((tag) => {
                var elements = this.#form.getElementsByTagName(tag);
                for (var i = 0; i < elements.length; i++) {
                    var element = elements[i];
                    var include_element = true;

                    // check, if it should be excluded
                    // sometimes it is required, to exclude elements from the change event listener
                    // of the reading of elements.
                    if (this.#form_exclude) {
                        if (this.#form_exclude.indexOf(element.id) > -1
                            || this.#form_exclude.indexOf(element.name) > -1) {
                            // don't put that into the registered element list
                            include_element = false;
                        }
                    }

                    if (include_element) {
                        this.#form_elements.push(elements[i]);
                    }
                }
            });

            // now, add a onChange to every element
            this.#form_elements.map((element) => {
                if (element.tagName == "INPUT" &&
                    element.type.toLowerCase() == "submit") {
                    // we should not put this.element to the list
                } else {
                    element.onchange = this.event_changed.bind(this);
                }
            });

            // block submission of the form
            this.#form.addEventListener("submit", function (event) {
                event.preventDefault(); // Prevent full-page reload
            });

            returnValue = true;
        }
        return returnValue;
    }

    initialize() {
        if (!this.#initialized && this.registerElements()) {

            var missing_functions = [[this.#action_functions.init, "init"],
            [this.#action_functions.selected, "selected"],
            [this.#action_functions.new, "new"],
            [this.#action_functions.save, "save"],
            [this.#action_functions.delete, "delete"],
            [this.#action_functions.changed, "changed"],
            [this.#action_functions.validate, "validate"],
            [this.#action_functions.refreshView, "refreshView"],
            [this.#action_functions.removedOrAdded, "removeOrAdded"],
            [this.#action_functions.initSearch, "initSearch"],
            [this.#action_functions.search, "search"]
            ];

            var errors = [];
            missing_functions.map((x) => {
                if (!x[0]) {
                    errors.push(x[1]);
                }
            });
            if (errors.length > 0) {
                console.error("CRUDsm: action functions missing: " + errors.join(", ") + "! please add them.");
            } else {
                this.#initialized = true;
            }
        }
    }
    isInitialized() {
        return this.#initialized;
    }

    /**
     * actions
     */
    actionInit(func) {
        this.#action_functions.init = func;
        return this;
    }
    actionSelected(func) {
        this.#action_functions.selected = func;
        return this;
    }
    actionNew(func) {
        this.#action_functions.new = func;
        return this;
    }
    actionSave(func) {
        this.#action_functions.save = func;
        return this;
    }
    actionDelete(func) {
        this.#action_functions.delete = func;
        return this;
    }
    actionChanged(func) {
        this.#action_functions.changed = func;
        return this;
    }
    actionValidate(func) {
        this.#action_functions.validate = func;
        return this;
    }
    actionRefreshView(func) {
        this.#action_functions.refreshView = func;
        return this;
    }
    actionRemovedOrAdded(func) {
        this.#action_functions.removedOrAdded = func;
        return this;
    }
    actionInitSearch(func) {
        this.#action_functions.initSearch = func;
        return this;
    }
    actionSearch(func) {
        this.#action_functions.search = func;
        return this;
    }
    /**
     * events
     */
    event_init() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.INITIALIZE);
    }
    event_new() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.NEW);
    }
    event_select() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.SELECT);
    }
    event_save() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.SAVE);
    }
    event_delete() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.DELETE);
    }
    event_changed() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.CHANGED);
    }
    event_closing() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.CLOSING);
    }
    event_initSearch() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.INIT_SEARCH);
    }
    event_search() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.SEARCH);
    }
    event_validate() {
        return this.#fireEvent(CRUDsm.#CRUDEvents.VALIDATE);
    }

    // initialise the components
    async #fireEvent(event) {
        var returnValue = false;
        if (!this.#initialized) {
            console.error("not initialized");
            return false;
        } else if (this.#is_in_refresh) {
            // there is an event fired, while we are in refresh state (or running through the state machine)
        } else {
            try {
                this.#is_in_refresh = true;
                var action_stack = [];
                switch (event) {
                    case CRUDsm.#CRUDEvents.INITIALIZE:
                        // in that case, we should clear all the stuff from before
                        action_stack.push(CRUDsm.#StateMachine.INITIALIZE);
                        action_stack.push(CRUDsm.#StateMachine.SELECTIONLISTCHANGED);
                        if ( this.#flag_select_initial_pos > -1 ) {
                            action_stack.push(CRUDsm.#StateMachine.PRESELECTED);
                        } else {
                            action_stack.push(CRUDsm.#StateMachine.NOTHING_SELECTED);
                        }
                        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        break;
                    case CRUDsm.#CRUDEvents.NEW:
                        if (this.#changeFlag) {
                            action_stack.push(CRUDsm.#StateMachine.AUTOMATICSAVE);
                        }
                        action_stack.push(CRUDsm.#StateMachine.NEW);
                        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        break;
                    case CRUDsm.#CRUDEvents.SELECT:
                        action_stack.push(CRUDsm.#StateMachine.PRESELECTED);
                        if (this.#changeFlag) {
                            action_stack.push(CRUDsm.#StateMachine.AUTOMATICSAVE);
                        }
                        action_stack.push(CRUDsm.#StateMachine.SELECTED);
                        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        break;
                    case CRUDsm.#CRUDEvents.VALIDATE:
                        action_stack.push(CRUDsm.#StateMachine.VALIDATING);
                        break;
                    case CRUDsm.#CRUDEvents.SAVE:
                        if (this.#changeFlag) {
                            action_stack.push(CRUDsm.#StateMachine.VALIDATING);
                            action_stack.push(CRUDsm.#StateMachine.SAVE);
                            action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        }
                        break;
                    case CRUDsm.#CRUDEvents.DELETE:
                        action_stack.push(CRUDsm.#StateMachine.DELETE);
                        action_stack.push(CRUDsm.#StateMachine.NOTHING_SELECTED);
                        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        break;
                    case CRUDsm.#CRUDEvents.CHANGED:
                        if (!this.#isInSearch && !this.#changeFlag) {
                            /*
                            * search   flag    result
                            *  0       0       1
                            *  1       0       0
                            *  0       1       0
                            *  1       1       -> should never happen, but 0
                            */
                            action_stack.push(CRUDsm.#StateMachine.CHANGED);

                        }
                        break;
                    case CRUDsm.#CRUDEvents.CLOSING:
                        if (this.#changeFlag) {
                            action_stack.push(CRUDsm.#StateMachine.AUTOMATICSAVE);
                            action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        }
                        break;
                    case CRUDsm.#CRUDEvents.INIT_SEARCH:
                        if (this.#changeFlag) {
                            action_stack.push(CRUDsm.#StateMachine.AUTOMATICSAVE);
                            action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        }
                        action_stack.push(CRUDsm.#StateMachine.INIT_SEARCH);
                        this.#isInSearch = true;
                        break;
                    case CRUDsm.#CRUDEvents.SEARCH:
                        action_stack.push(CRUDsm.#StateMachine.SEARCH);
                        action_stack.push(CRUDsm.#StateMachine.SELECTIONLISTCHANGED);
                        action_stack.push(CRUDsm.#StateMachine.NOTHING_SELECTED);
                        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
                        this.#isInSearch = false;
                        break;
                    default:
                        console.error("CRUDsm - fireEvent: unknown event " + event + "!");
                }

                returnValue = await this.#process(action_stack);
            } catch (err) {
                console.error(err);
            } finally {
                this.#is_in_refresh = false;
            }
        }

        return returnValue;
    }

    async #process(stack) {
        var returnValue = true;
        while (stack.length > 0 && returnValue) {

            var action = stack.shift()

            switch (action) {
                /*
                * .actionInit( () => {} )
                * .actionSelected( () => {} )
                * .actionNew( () => {} )
                * .actionSave( () => {} )
                * .actionDelete( () => {} )
                * .actionChanged( () => {} )
                * .actionValidate( () => {} )
                * .actionRefreshView( () => {} )
                * .actionRemovedOrAdded( () => {} )
                * .actionInitSearch( () => {} )
                * .actionSearch( () => {} ) 
                */
                case CRUDsm.#StateMachine.INITIALIZE:
                    {
                        // Get result (could be sync or async)
                        const result = this.#action_functions.init();

                        // Wait for resolution if it's a Promise
                        const objects = result instanceof Promise ? await result : result;
                        this.#tuples = new CRUDTuples();
                        if (Array.isArray(objects)) {
                            // transfer this elements to the tuples
                            objects.map((obj) => {
                                this.#tuples.add(obj);
                            })
                            this.#initialized_by_event = true;
                            returnValue = true;
                        } else {
                            returnValue = false;
                        }
                    }
                    break;
                case CRUDsm.#StateMachine.NOTHING_SELECTED:
                    {
                        returnValue = true;
                        this.#changeFlag = false;
                        this.#tuples.current = null;
                    }
                    break;
                case CRUDsm.#StateMachine.PRESELECTED:
                    {
                        // this is only for the initial selectFirst flagg
                        if( this.#flag_select_initial_pos > -1) {
                            this.#tuples.selected = this.#flag_select_initial_pos;
                            this.#flag_select_initial_pos = -1;
                        }
                        
                        const result = this.#action_functions.selected(this.#tuples.selected);
                        this.#selectedTupleID = result instanceof Promise ? await result : result;
                    }
                    break;
                case CRUDsm.#StateMachine.SELECTED:
                    {
                        this.#tuples.current = this.#selectedTupleID;
                        this.#changeFlag = false;
                    }
                    break;
                case CRUDsm.#StateMachine.NEW:
                    {
                        const result = this.#action_functions.new();
                        const tmp_object = result instanceof Promise ? await result : result;

                        if (!tmp_object) {
                            // no new value, so break up here
                            returnValue = false;
                        } else {
                            // this object should be stored in the tuples right now, because it might
                            // be, that we delete it.
                            this.#new_object = tmp_object;

                            // reset the current tuple
                            this.#tuples.current = null;

                            returnValue = true;
                        }
                    }
                    break;
                case CRUDsm.#StateMachine.CHANGED:
                    {
                        this.#changeFlag = true;
                        const result = this.#action_functions.changed();
                        if (result instanceof Promise) await result;  // to block the proceeding

                        if (this.#tuples.current) {
                            // if the object is changed, then we need to take a copy of the 
                            // original value. from here on, we will use the copy, until we
                            // store it to the db.
                            this.#new_object = JSON.parse(JSON.stringify(this.#tuples.current.obj));
                        }

                        returnValue = true;
                    }
                    break;
                case CRUDsm.#StateMachine.SAVE:
                    {
                        var object_to_save = null;
                        var add_flagg = false;

                        // object to save is always in the new_object stored
                        object_to_save = this.#new_object;

                        // depending, if there is a new element 
                        if (!this.#tuples.getCurrentId()) {
                            add_flagg = true;
                        }

                        const result = this.#action_functions.save(object_to_save, this.#tuples.getCurrentId());
                        object_to_save = result instanceof Promise ? await result : result;

                        if (object_to_save) {
                            // element was saved, so store back
                            if (add_flagg) {
                                // this is a new element. so add to the system
                                this.#tuples.add(object_to_save);
                                // here we need to add a new action into the stack:
                                stack.unshift(CRUDsm.#StateMachine.SELECTIONLISTCHANGED);
                            } else {
                                this.#tuples.change(this.#tuples.current.id, object_to_save);
                            }
                            this.#changeFlag = false;
                        }
                    }
                    break;
                case CRUDsm.#StateMachine.AUTOMATICSAVE:
                    if (this.#automatic_save) {
                        stack.unshift(CRUDsm.#StateMachine.SAVE);
                        stack.unshift(CRUDsm.#StateMachine.VALIDATING);
                        returnValue = true;
                    } else {
                        // todo: a question should appear here if to be stored,
                        // canceled or deleted
                    }
                    break;
                case CRUDsm.#StateMachine.DELETE:
                    // here the false clause should be replaced by a popup window
                    // asking, if the object should really be deleted or not.
                    // todo: replace that false and include a question popup
                    if (false) {

                        returnValue = false;
                    } else {
                        if (this.#tuples.current === null) {
                            // this was a new object, so we don't do anything
                            this.#new_object = null;
                            returnValue = true;
                            this.#changeFlag = false;
                        } else {

                            const result = this.#action_functions.delete(this.#tuples.current.obj);
                            const delete_or_not = result instanceof Promise ? await result : result;

                            if (delete_or_not) {
                                this.#tuples.delete(this.#tuples.current.id);
                                this.#changeFlag = false;
                                returnValue = true;

                                stack.unshift(CRUDsm.#StateMachine.SELECTIONLISTCHANGED);
                            } else {
                                // not deleted
                                returnValue = false;
                            }
                        }
                    }
                    break;
                case CRUDsm.#StateMachine.REFRESHVIEW:
                    {
                        var tuple = null;

                        if (this.#changeFlag || !this.#tuples.getCurrentId()) {
                            tuple = this.#new_object;
                        } else {
                            tuple = this.#tuples.current;
                            if (tuple) {
                                // if the current is existing, then the tuple is filled otherwise it is not
                                tuple = tuple.obj;
                            }
                        }

                        const result = this.#action_functions.refreshView(tuple);
                        if (result instanceof Promise) await result;

                        this.#action_functions.refreshView(tuple);
                        returnValue = true;
                    }

                    break;
                case CRUDsm.#StateMachine.VALIDATING:
                    {
                        if (!this.#changeFlag) {
                            // validation only valid, if change flag set
                            console.error("CRUDsm: validation without change event fired!");
                            returnValue = false;
                        } else {
                            const result = this.#action_functions.validate(this.#new_object);
                            const tmpObj = result instanceof Promise ? await result : result;

                            if (tmpObj) {
                                this.#new_object = tmpObj;
                                returnValue = true;
                            } else {
                                returnValue = false;
                            }
                        }
                    }
                    break;
                case CRUDsm.#StateMachine.SELECTIONLISTCHANGED:
                    {
                        const result = this.#action_functions.removedOrAdded(this.#tuples.getLastActionID());
                        if (result instanceof Promise) await result;
                    }
                    break;
                case CRUDsm.#StateMachine.INIT_SEARCH:
                    {
                        const result = this.#action_functions.initSearch();
                        if (result instanceof Promise) await result;
                    }
                    break;
                case CRUDsm.#StateMachine.SEARCH:
                    {
                        // this is returning all new object of the tuples
                        const result = this.#action_functions.search();
                        var list = result instanceof Promise ? await result : result;
                        this.#action_functions
                        if (list) {
                            this.#tuples = new CRUDTuples();
                            // add all object into the tuples
                            list.map((x) => this.#tuples.add(x));
                            returnValue = true;
                        } else {
                            returnValue = false;
                        }
                    }
                    break;
                default:
                    console.error("CRUDsm - #process: unknown action " + action + "!");
            }
        }
        return returnValue;
    }

    // Returns the HTML element from #form_elements by name or id
    getElement(name) {
        return this.getElements(name)[0] || null;
    }

    // Returns all matching elements by name or id
    getElements(name) {
        return this.#form_elements.filter(el =>
            el?.name === name || el?.id === name
        );
    }

    /// Adds a class to all matching elements
    setClass(name, class_name) {
        this.#setAndUnsetClass(name, class_name, true);
    }
    unsetClass(name, class_name) {
        this.#setAndUnsetClass(name, class_name, false);
    }
    #setAndUnsetClass(name, class_name, set_or_unset) {
        const elements = this.getElements(name);
        elements.forEach(el => {
            if (set_or_unset) {
                if (el && !el.classList.contains(class_name)) {
                    el.classList.add(class_name);
                }
            } else {
                if (el && el.classList.contains(class_name)) {
                    el.classList.remove(class_name);
                }
            }
        });
    }

    setLabelForInput(name, class_name) {
        this.#setAndUnsetLabelForInput(name, class_name, true);
    };
    unsetLabelForInput(name, class_name) {
        this.#setAndUnsetLabelForInput(name, class_name, false);
    }
    #setAndUnsetLabelForInput(name, class_name, set_or_unset) {
        const elements = this.getElements(name);
        elements.forEach(el => {
            const label_for_element = this.#getLabelForInput(el);

            if (set_or_unset) {
                if (label_for_element && !label_for_element.classList.contains(class_name)) {
                    label_for_element.classList.add(class_name)
                }
            } else {
                if (label_for_element && label_for_element.classList.contains(class_name)) {
                    label_for_element.classList.remove(class_name);
                }
            }
        });
    }
    #getLabelForInput(inputElement) {
        if (!inputElement) return null;

        // 1. Check for label[for=id]
        if (inputElement.id) {
            const label = document.querySelector(`label[for="${inputElement.id}"]`);
            if (label) return label;
        }

        // 2. Fallback: look for parent label (wrapping)
        let parent = inputElement.parentElement;
        while (parent) {
            if (parent.tagName?.toLowerCase() === 'label') {
                return parent;
            }
            parent = parent.parentElement;
        }

        return null;
    }

    // editor helpers
    getField(name) {
        var element = this.#form_elements.find((x) => {
            return x.name === name || x.id === name;
        });
        var rv = null;
        if (element) {
            var tagName = element.tagName.toLowerCase();
            var tagType = element.type.toLowerCase();
            if (tagName === "textarea") {
                rv = element.value;
            } else if (tagName === "input") {

                switch (tagType) {
                    case "text":
                    case "number":
                    case "hidden":
                    case "button":
                    case "submit":
                    case "tel":
                        rv = element.value;
                        break;
                    case "checkbox":
                        rv = element.checked;
                        break;
                    case "color":
                    case "date":
                    case "datetime-local":
                    case "email":
                    case "file":
                    case "image":
                    case "month":
                    case "password":
                    case "radio":
                        {   // radio is different. while all elements do have the same name,
                            // the values and id's differ. for that reason we need to 
                            // search all names and check, if there is a value like that to be
                            // checked.
                            this.#form_elements.map((e) => {
                                if (e.name === name &&
                                    e.checked) {
                                    rv = e.value;
                                }
                            });
                        }
                        break;
                    case "range":
                    case "reset":
                    case "search":
                    case "time":
                    case "url":
                    case "week":
                        console.error("CRUDsm " + tagType);
                        break;
                }
            } else if (tagName === "select") {
                var i = element.selectedIndex;
                if (i > -1) {
                    rv = element.options[i].value;
                }
            }
        }
        return rv;
    }
    setField(name, value) {
        var element = this.#form_elements.find((x) => {
            return x.name === name || x.id === name;
        });
        if (element) {
            var tagName = element.tagName.toLowerCase();
            var tagType = element.type.toLowerCase();
            if (tagName === "textarea") {
                if (value) {
                    element.value = value;
                } else {
                    element.value = "";
                }
            } else if (tagName === "input") {

                switch (tagType) {
                    case "text":
                    case "number":
                    case "hidden":
                    case "button":
                    case "submit":
                    case "tel":
                        if (value) {
                            element.value = value;
                        } else {
                            element.value = "";
                        }
                        break;
                    case "checkbox":
                        if (value) {
                            element.checked = true;
                        } else {
                            element.checked = false;
                        }
                        break;
                    case "color":
                    case "date":
                    case "datetime-local":
                    case "email":
                    case "file":
                    case "image":
                    case "month":
                    case "password":
                    case "radio":
                        {   // radio is different. while all elements do have the same name,
                            // the values and id's differ. for that reason we need to 
                            // search all names and check, if there is a value like that to be
                            // checked.
                            this.#form_elements.map((e) => {
                                if (e.name === name) {
                                    if (e.value === value) {
                                        e.checked = true;
                                    } else {
                                        e.checked = false;
                                    }
                                }
                            });
                        }
                        break;
                    case "range":
                    case "reset":
                    case "search":
                    case "time":
                    case "url":
                    case "week":
                        console.error("CRUDsm " + tagType);
                        break;
                }
            } else if (tagName === "select") {
                var index_value = -1;
                for (var i = 0; i < element.options.length; i++) {
                    if (element.options[i].value == value) {
                        index_value = i;
                    }
                }
                element.selectedIndex = index_value;
            }
        }
        return this;
    }
    deactivate(names) {
        if (Array.isArray(names)) {
            // change nothing, because we will map through ...
        } else {
            names = [names];
        }
        names.map((name) => {
            var element = document.getElementById(name);
            if (element) {
                if (element.tagName === "input") {
                    // not disable, because it will not submit the value to the server
                    element.readOnly = true;
                } else {
                    element.disabled = true;
                }
            }
        })
        return this;
    }
    activate(names) {
        if (Array.isArray(names)) {
            // change nothing, because we will map through ...
        } else {
            names = [names];
        }
        names.map((name) => {
            var element = document.getElementById(name);
            if (element) {
                if (element.tagName === "input") {
                    element.readOnly = false;
                } else {
                    element.disabled = false;
                }
            }
        })
    }
    getItems() {
        return this.#tuples.tuples;
    }
    getItem(id) {
        return this.#tuples.get(id);
    }
    set selected(val) {
        this.#tuples ? this.#tuples.selected = val : this.#flag_select_initial_pos = val;
    }
    get selected() {
        return this.#tuples ? this.#tuples.selected : null;
    }
    getCurrent() {
        var obj = undefined;
        if (this.#changeFlag) {
            this.event_validate();
            obj = this.#new_object;
        } else if (this.#tuples.current) {
            obj = this.#tuples.current.obj;
        }
        return obj;
    }
    setCurrent(obj) {
        this.event_changed();
        if (this.#changeFlag) {
            this.#new_object = obj;
        } else if (this.#tuples.current) {
            this.#tuples.current.obj = obj;
        }
        var action_stack = [];
        action_stack.push(CRUDsm.#StateMachine.REFRESHVIEW);
        this.#process(action_stack);
    }
}
