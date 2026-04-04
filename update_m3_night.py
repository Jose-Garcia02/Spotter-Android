import xml.etree.ElementTree as ET
def update_colors(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        m3_colors = {
            'md_theme_primary': '#A3C2FF',
            'md_theme_onPrimary': '#002E69',
            'md_theme_primaryContainer': '#004494',
            'md_theme_onPrimaryContainer': '#D6E4FF',
            'md_theme_secondary': '#81DBA3',
            'md_theme_onSecondary': '#00391A',
            'md_theme_secondaryContainer': '#005228',
            'md_theme_onSecondaryContainer': '#9DF7BE',
            'md_theme_error': '#FFB4AB',
            'md_theme_onError': '#690005',
            'md_theme_errorContainer': '#93000A',
            'md_theme_onErrorContainer': '#FFDAD6',
            'md_theme_background': '#1A1C1E',
            'md_theme_onBackground': '#E2E2E6',
            'md_theme_surface': '#1A1C1E',
            'md_theme_onSurface': '#E2E2E6',
            'md_theme_surfaceVariant': '#43474E',
            'md_theme_onSurfaceVariant': '#C3C6CF',
            'md_theme_outline': '#8D9199',
        }
        existing_colors = {child.attrib['name'] for child in root.findall('color')}
        for name, value in m3_colors.items():
            if name not in existing_colors:
                elem = ET.Element('color', {'name': name})
                elem.text = value
                root.append(elem)
            else:
                for elem in root.findall('color'):
                    if elem.attrib['name'] == name:
                         elem.text = value
        tree.write(file_path, xml_declaration=True, encoding='utf-8')
    except Exception as e:
        print(f"Error updating {file_path}: {e}")
update_colors('app/src/main/res/values-night/colors.xml')
