import xml.etree.ElementTree as ET
def update_colors(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        # Add md_theme_... colors if they don't exist
        m3_colors = {
            'md_theme_primary': '#2563EB',
            'md_theme_onPrimary': '#FFFFFF',
            'md_theme_primaryContainer': '#DBEAFE',
            'md_theme_onPrimaryContainer': '#1E3A8A',
            'md_theme_secondary': '#16A34A',
            'md_theme_onSecondary': '#FFFFFF',
            'md_theme_secondaryContainer': '#DCFCE7',
            'md_theme_onSecondaryContainer': '#14532D',
            'md_theme_error': '#DC2626',
            'md_theme_onError': '#FFFFFF',
            'md_theme_errorContainer': '#FEE2E2',
            'md_theme_onErrorContainer': '#7F1D1D',
            'md_theme_background': '#F9F9F9',
            'md_theme_onBackground': '#1A1C1E',
            'md_theme_surface': '#FFFFFF',
            'md_theme_onSurface': '#1A1C1E',
            'md_theme_surfaceVariant': '#E7E0EC',
            'md_theme_onSurfaceVariant': '#49454F',
            'md_theme_outline': '#79747E',
        }
        existing_colors = {child.attrib['name'] for child in root.findall('color')}
        for name, value in m3_colors.items():
            if name not in existing_colors:
                elem = ET.Element('color', {'name': name})
                elem.text = value
                root.append(elem)
        tree.write(file_path, xml_declaration=True, encoding='utf-8')
    except Exception as e:
        print(f"Error updating {file_path}: {e}")
update_colors('app/src/main/res/values/colors.xml')
update_colors('app/src/main/res/values-night/colors.xml')
