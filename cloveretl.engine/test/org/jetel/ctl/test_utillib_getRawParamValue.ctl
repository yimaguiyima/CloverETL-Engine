map[string, string] params;
string nonExisting = "NONEXISTING";
string ret1;
string ret2;

function integer transform() {
	foreach(string paramName: getRawParamValues().getKeys()) {
		params[paramName] = getRawParamValue(paramName);
	}
	params[nonExisting] = getParamValue(nonExisting);
	string str = null;
	ret1 = getParamValue(null);
	ret2 = getParamValue(str);
	return 0;
}