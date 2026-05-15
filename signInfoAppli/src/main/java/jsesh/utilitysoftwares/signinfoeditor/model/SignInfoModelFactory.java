package jsesh.utilitysoftwares.signinfoeditor.model;

public interface SignInfoModelFactory {
    /**
     * Build a default model, with the signs defined in the current JSesh installation.
     * @return
     */
    SignInfoModel buildDefaultModel();
}
