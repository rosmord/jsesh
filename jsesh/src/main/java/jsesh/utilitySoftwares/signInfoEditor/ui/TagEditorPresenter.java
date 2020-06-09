package jsesh.utilitySoftwares.signInfoEditor.ui;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoModel;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;

/**
 * Editor logic for tags.
 * 
 * @author rosmord
 */
public class TagEditorPresenter {
	/**
	 * The tag editor.
	 */

	private JTagEditor jTagEditor;

	private SignInfoModel signInfoModel;

	private TagListModel tagListModel;
	private TagNameTableModel tagNameTableModel;

	public TagEditorPresenter(SignInfoModel signInfoModel) {

		jTagEditor = new JTagEditor();
		// bindings.
		// a) for the tag list
		new GrowableListControl(jTagEditor.getTagNameList(), jTagEditor
				.getTagAddButton(), jTagEditor.getTagRemoveButton(), jTagEditor
				.getTagNameField());

		// b) for the tag label
		new GrowableTableControl(jTagEditor.getLabelTable(), jTagEditor
				.getLabelAddButton(), jTagEditor.getLabelRemoveButton(),
				jTagEditor.getLabelField());

		jTagEditor.getTagNameList().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						changeTag();
					}
				});
		setSignInfoModel(signInfoModel);

	}

	private void changeTag() {
		tagNameTableModel = new TagNameTableModel(getSelectedTag());
		jTagEditor.getLabelTable().setModel(tagNameTableModel);

		JComboBox langCB = new JComboBox();
		for (int i = 0; i < SignDescriptionConstants.LANGUAGES_CODES.length; i++) {
			langCB.addItem(SignDescriptionConstants.LANGUAGES_CODES[i]);
		}
		jTagEditor.getLabelTable().getColumnModel().getColumn(0).setCellEditor(
				new DefaultCellEditor(langCB));
	}

	public JTagEditor getJTagEditor() {
		return jTagEditor;
	}

	public String getSelectedTag() {
		return (String) jTagEditor.getTagNameList().getSelectedValue();
	}

	private class TagListModel extends AbstractListModel implements
			GrowableModel {
		private static final long serialVersionUID = 1L;
		private List tagList;

		public TagListModel() {
			tagList = signInfoModel.getTags();
		}

		public Object getElementAt(int index) {
			return tagList.get(index);
		}

		public int getSize() {
			return tagList.size();
		}

		public void addRow(String txt) {
			if (txt != null && !"".equals(txt)
					&& !signInfoModel.definesTag(txt)) {
				signInfoModel.addTagCategory(txt, true);
				tagList.add(txt);
				fireIntervalAdded(this, tagList.size() - 1, tagList.size() - 1);
			}
		}

		public boolean removeRow(int row) {
			if (canRemove(row)) {
				tagList.remove(row);
				fireIntervalRemoved(this, row, row);
				return true;
			} else
				return false;
		}
		
		public boolean canRemove(int row) {
			String tag= getSelectedTag();
			return (tag != null && signInfoModel.isUserTag(tag) && signInfoModel.removeTag(tag));
		}
	}

	private class TagNameTableModel extends AbstractTableModel implements
			GrowableModel {

		private static final long serialVersionUID = 1L;

		private String currentTag;

		private List properties;

		private String[] columnNames = { "Lang", "Label" };

		private String languages[] = { "en" };

		public TagNameTableModel(String currentTag) {
			this.currentTag = currentTag;
			if (currentTag != null) {
				properties = signInfoModel.getLabelsForTag(currentTag);
			} else {
				properties = Collections.EMPTY_LIST;
			}
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return properties.size();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return getXMLInfoProperty(rowIndex).isUserDefinition();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			XMLInfoProperty property = getXMLInfoProperty(rowIndex);
			switch (columnIndex) {
			case 0:
				return property.get(SignDescriptionConstants.LANG);
			case 1:
				return property.get(SignDescriptionConstants.LABEL);
			default:
				throw new ArrayIndexOutOfBoundsException(columnIndex);
			}
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			XMLInfoProperty property = getXMLInfoProperty(rowIndex);
			switch (columnIndex) {
			case 0:
				property.setAttribute(SignDescriptionConstants.LANG,
						(String) value);
				break;
			case 1:
				property.setAttribute(SignDescriptionConstants.LABEL,
						(String) value);
				break;
			default:
				throw new ArrayIndexOutOfBoundsException(columnIndex);
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		private XMLInfoProperty getXMLInfoProperty(int rowIndex) {
			return (XMLInfoProperty) properties.get(rowIndex);
		}

		public void addRow(String txt) {
			if (getSelectedTag() != null) {
				XMLInfoProperty prop = new XMLInfoProperty(
						SignDescriptionConstants.TAG_LABEL, true);
				prop.setAttribute(SignDescriptionConstants.TAG,
						getSelectedTag());
				prop.setAttribute(SignDescriptionConstants.LANG, "en");
				prop.setAttribute(SignDescriptionConstants.LABEL, txt);
				signInfoModel.addTagLabel(prop);
				properties.add(prop);
				fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
			}
		}

		public boolean removeRow(int row) {
			int i = jTagEditor.getLabelTable().getSelectedRow();
			if (i != -1) {
				XMLInfoProperty prop = getXMLInfoProperty(i);
				if (prop.isUserDefinition()) {
					signInfoModel.removeLabel(prop);
					properties.remove(i);
					fireTableRowsDeleted(i, i);
					return true;
				} else
					return false;
			} else
				return false;
		}
		
		public boolean canRemove(int row) {
			boolean result= false;
			int i = jTagEditor.getLabelTable().getSelectedRow();
			if (i != -1) {
				XMLInfoProperty prop = getXMLInfoProperty(i);
				result = prop.isUserDefinition();
			}
			return result;
		}

	}

	public void setSignInfoModel(SignInfoModel signInfoModel) {
		this.signInfoModel = signInfoModel;
		tagListModel = new TagListModel();
		jTagEditor.getTagNameList().setModel(tagListModel);
		jTagEditor.getLabelTable().setModel(new TagNameTableModel(null));
	}
}
