package model;

public class Organizam {

	private String tax_id;
	private String name_txt;
	private String unique_name;
	private String name_class;
	private String parent_tax_id;
	private String rank;
	private String embl_code;
	private String division_id;
	private String inherited_div_flag;
	private String genetic_code_id;
	private String inherited_GC_flag;
	private String mitochondrial_genetic_code_id;
	private String inheritedMGC_flag;
	private String genbank_hidden_flag;
	private String hidden_subtree_root_flag;
	private String division_cde;
	private String division_name;
	private String abbrevation;
	private String name;
	private String cde;
	private String starts;
	private String file_location;
	
	public String getTax_id() {
		return tax_id;
	}
	public void setTax_id(String tax_id) {
		this.tax_id = tax_id;
	}
	public String getName_txt() {
		return name_txt;
	}
	public void setName_txt(String name_txt) {
		this.name_txt = name_txt;
	}
	public String getUnique_name() {
		return unique_name;
	}
	public void setUnique_name(String unique_name) {
		this.unique_name = unique_name;
	}
	public String getName_class() {
		return name_class;
	}
	public void setName_class(String name_class) {
		this.name_class = name_class;
	}
	public String getParent_tax_id() {
		return parent_tax_id;
	}
	public void setParent_tax_id(String parent_tax_id) {
		this.parent_tax_id = parent_tax_id;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public String getEmbl_code() {
		return embl_code;
	}
	public void setEmbl_code(String embl_code) {
		this.embl_code = embl_code;
	}
	public String getDivision_id() {
		return division_id;
	}
	public void setDivision_id(String division_id) {
		this.division_id = division_id;
	}
	public String getInherited_div_flag() {
		return inherited_div_flag;
	}
	public void setInherited_div_flag(String inherited_div_flag) {
		this.inherited_div_flag = inherited_div_flag;
	}
	public String getGenetic_code_id() {
		return genetic_code_id;
	}
	public void setGenetic_code_id(String genetic_code_id) {
		this.genetic_code_id = genetic_code_id;
	}
	public String getInherited_GC_flag() {
		return inherited_GC_flag;
	}
	public void setInherited_GC_flag(String inherited_GC_flag) {
		this.inherited_GC_flag = inherited_GC_flag;
	}
	public String getMitochondrial_genetic_code_id() {
		return mitochondrial_genetic_code_id;
	}
	public void setMitochondrial_genetic_code_id(String mitochondrial_genetic_code_id) {
		this.mitochondrial_genetic_code_id = mitochondrial_genetic_code_id;
	}
	public String getInheritedMGC_flag() {
		return inheritedMGC_flag;
	}
	public void setInheritedMGC_flag(String inheritedMGC_flag) {
		this.inheritedMGC_flag = inheritedMGC_flag;
	}
	public String getGenbank_hidden_flag() {
		return genbank_hidden_flag;
	}
	public void setGenbank_hidden_flag(String genbank_hidden_flag) {
		this.genbank_hidden_flag = genbank_hidden_flag;
	}
	public String getHidden_subtree_root_flag() {
		return hidden_subtree_root_flag;
	}
	public void setHidden_subtree_root_flag(String hidden_subtree_root_flag) {
		this.hidden_subtree_root_flag = hidden_subtree_root_flag;
	}
	public String getDivision_cde() {
		return division_cde;
	}
	public void setDivision_cde(String division_cde) {
		this.division_cde = division_cde;
	}
	public String getDivision_name() {
		return division_name;
	}
	public void setDivision_name(String division_name) {
		this.division_name = division_name;
	}
	public String getAbbrevation() {
		return abbrevation;
	}
	public void setAbbrevation(String abbrevation) {
		this.abbrevation = abbrevation;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCde() {
		return cde;
	}
	public void setCde(String cde) {
		this.cde = cde;
	}
	public String getStarts() {
		return starts;
	}
	public void setStarts(String starts) {
		this.starts = starts;
	}
	public String getFile_location() {
		return file_location;
	}
	public void setFile_location(String file_location) {
		this.file_location = file_location;
	}
	
	/*
	 * dataInfo.put("tax_id", "Node id in GenBank taxonomy database");
		dataInfo.put("name_txt", "Name itself");
		dataInfo.put("unique_name", "The unique variant of this name if name not unique");
		dataInfo.put("parent_tax_id", "Parent node id in GenBank taxonomy database");
		dataInfo.put("abbrevation", "Genetic code name abbreviation");
		dataInfo.put("cde", "Translation table for this genetic code");
		dataInfo.put("division_cde", "GenBank division code (three characters)");
		dataInfo.put("division_name", "E.g. BCT, PLN, VRT, MAM, PRI...");
		dataInfo.put("division_id", "Taxonomy database division id");
		dataInfo.put("embl_code", "Locus-name prefix; not unique");
		dataInfo.put("genbank_hidden_flag", "1 if name is suppressed in GenBank entry lineage");
		dataInfo.put("genetic_code_id", "GenBank genetic code id");
		dataInfo.put("hidden_subtree_root_flag", "1 if this subtree has no sequence data yet");
		dataInfo.put("inherited_div_flag", "1 if node inherits division from parent");
		dataInfo.put("inherited_GC_flag", "1 if node inherits genetic code from parent");
		dataInfo.put("inheritedMGC_flag", "1 if node inherits mitochondrial gencode from parent");
		dataInfo.put("mitochondrial_genetic_code_id", "Mitochonrial GenBank genetic code id");
		dataInfo.put("name", "Genetic code name");
		dataInfo.put("name_class", "(Synonym, common name, ...)");
		dataInfo.put("rank", "Rank of this node (superkingdom, kingdom, ...)");
		dataInfo.put("starts", "Start codons for this genetic code");
	 */
	
	
}
